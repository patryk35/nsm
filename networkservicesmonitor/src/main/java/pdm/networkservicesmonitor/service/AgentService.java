package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.exceptions.BadRequestException;
import pdm.networkservicesmonitor.exceptions.MethodNotAllowed;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.AgentConfiguration;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.payload.agent.AgentConfigurationResponse;
import pdm.networkservicesmonitor.payload.agent.AgentDataPacket;
import pdm.networkservicesmonitor.payload.agent.AgentRequest;
import pdm.networkservicesmonitor.payload.agent.ServiceConfiguration;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateRequest;
import pdm.networkservicesmonitor.payload.client.agent.AgentResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddLogsConfiguration;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceCreateRequest;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.repository.CollectedLogsRepository;
import pdm.networkservicesmonitor.repository.LogsCollectingConfigurationRepository;
import pdm.networkservicesmonitor.repository.ServiceRepository;
import pdm.networkservicesmonitor.security.jwt.JwtTokenProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AgentService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private LogsCollectingConfigurationRepository logsCollectingConfigurationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CollectedLogsRepository collectedLogsRepository;


    public MonitorAgent createAgent(AgentCreateRequest agentCreateRequest) {
        MonitorAgent agent = new MonitorAgent(
                agentCreateRequest.getName(),
                agentCreateRequest.getDescription(),
                convertOriginsToList(agentCreateRequest.getAllowedOrigins())
        );

        return agentRepository.save(agent);
    }

    public pdm.networkservicesmonitor.model.agent.service.Service createService(ServiceCreateRequest serviceCreateRequest) {
        MonitorAgent agent = agentRepository.findById(serviceCreateRequest.getAgentId()).orElseThrow(() ->
                new NotFoundException("Agent not found. Agent id is not valid"));
        pdm.networkservicesmonitor.model.agent.service.Service service =
                new pdm.networkservicesmonitor.model.agent.service.Service(serviceCreateRequest.getName(), serviceCreateRequest.getDescription(),agent);
        agent.addService(service);
        if(agentRepository.save(agent) != null){
            return serviceRepository.save(service);
        }
        return null;
    }

    public LogsCollectingConfiguration addLogsCollectionConfiguration(ServiceAddLogsConfiguration serviceAddLogsConfiguration) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(serviceAddLogsConfiguration.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        LogsCollectingConfiguration logsCollectingConfiguration = new LogsCollectingConfiguration(serviceAddLogsConfiguration.getPath(),serviceAddLogsConfiguration.getMonitoredFilesMasks(),serviceAddLogsConfiguration.getUnmonitoredFileMasks(), service);
        service.addLogsCollectingConfiguration(logsCollectingConfiguration);
        if(serviceRepository.save(service) != null){
            return logsCollectingConfigurationRepository.save(logsCollectingConfiguration);
        }
        return null;
    }

    public void register(AgentRequest agentRequest, String requestIp) {
        MonitorAgent agent = agentRepository.findById(agentRequest.getAgentId()).orElseThrow(() ->
                new NotFoundException("Agent not found. Agent id or encryptionKey not valid"));

        if (agent.isRegistered()) {
            throw new MethodNotAllowed("Agent is already registered");
        }

        if (agent.getAllowedOrigins().isEmpty()) {
            agent.setAllowedOrigins(convertOriginsToList(requestIp));
        }

        agent.setRegistered(true);
        agentRepository.save(agent);
    }

    public PagedResponse<AgentResponse> getAllAgents(int page, int size) {
        validatePageNumberAndSize(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<MonitorAgent> agents = agentRepository.findAll(pageable);

        if (agents.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), agents.getNumber(),
                    agents.getSize(), agents.getTotalElements(), agents.getTotalPages(), agents.isLast());
        }
        List<AgentResponse> list = agents.getContent().stream()
                .map(e -> new AgentResponse(e.getId(), e.getName(), e.getDescription(), convertOriginsToString(e.getAllowedOrigins())))
                .collect(Collectors.toList());
        return new PagedResponse<>(list, agents.getNumber(),
                agents.getSize(), agents.getTotalElements(), agents.getTotalPages(), agents.isLast());
    }

    public AgentResponse getAgentById(UUID agentId) {
        MonitorAgent agent = agentRepository.findById(agentId).orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Agent Id","id",agentId));
        return new AgentResponse(agent.getId(), agent.getName(), agent.getDescription(), convertOriginsToString(agent.getAllowedOrigins()));
    }

    public AgentConfigurationResponse getAgentConfiguration(AgentRequest agentRequest, String authToken, String requestIp) {
        MonitorAgent monitorAgent = getAgentWithVerification(agentRequest.getAgentId(), authToken, requestIp);
        AgentConfiguration agentConfiguration = monitorAgent.getAgentConfiguration();
        List<ServiceConfiguration> servicesConfiguration = new ArrayList<>();
        monitorAgent.getServices().stream()
                .forEach(service -> servicesConfiguration.add(new ServiceConfiguration(
                        service.getId(),
                        service.getLogsCollectingConfigurations(),
                        service.getMonitoredParametersConfigurations()
                )));
        return new AgentConfigurationResponse(monitorAgent.getId(), agentConfiguration.getSendingInterval(),
                servicesConfiguration);
    }

    private boolean filterRequestIp(String requestIp, List<String> allowedOrigins) {
        if (allowedOrigins.contains(requestIp)) {
            return true;
        }
        if (allowedOrigins.contains("*")) {
            return true;
        }
        // TODO(high): check if requestIp is in allowedOrigins, some filter to check masks, partial addresses
        return false;
    }

    public boolean checkRegistrationStatus(AgentRequest agentRequest, String authToken, String requestIp) {
        return getAgentWithVerification(agentRequest.getAgentId(), authToken, requestIp).isRegistered();
    }

    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size cannot be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    private MonitorAgent getAgentWithVerification(UUID agentId, String authToken, String requestIp) {
        MonitorAgent agent = agentRepository.findById(agentId).orElseThrow(() -> new NotFoundException(String.format("Agent %s not found. Agent id or encryptionKey not valid", agentId.toString())));

        if (!jwtTokenProvider.validateAgentToken(authToken, agent.getEncryptionKey())) {
            throw new NotFoundException(String.format("Agent %s not found. Agent id or encryptionKey not valid", agentId.toString()));
        }

        if (!agent.getAllowedOrigins().isEmpty() && !filterRequestIp(requestIp, agent.getAllowedOrigins())) {
            throw new MethodNotAllowed("Current ip address not in allowed origins. Set appropriate allowed origins or left it blank to auto fill");
        }
        return agent;
    }


    private String convertOriginsToString(List<String> allowedOrigins) {
        StringBuilder sb = new StringBuilder();        allowedOrigins.stream().forEach(o -> {
            sb.append(o);
            sb.append(", ");
        });

        if (allowedOrigins.size() > 0) {
            int trimPosition = sb.lastIndexOf(",");
            sb.deleteCharAt(trimPosition);
            sb.deleteCharAt(trimPosition);
        }
        return sb.toString();
    }

    private List<String> convertOriginsToList(String allowedOrigins) {
        // TODO(high): should filter ip addresses and * and ip with mask
        return Pattern.compile(",").splitAsStream(allowedOrigins)
                .map(o -> o.trim())
                .filter(Predicate.not(o -> o.matches("^(|\\s+)$")))
                //.filter(o -> o.matches("^(\\*|{})"))
                .collect(Collectors.toList());
    }


    public void savePacket(AgentDataPacket agentDataPacket, String authToken, String requestIp) {
        MonitorAgent monitorAgent = getAgentWithVerification(agentDataPacket.getAgentId(), authToken, requestIp);
        agentDataPacket.getLogs().forEach(serviceLogs -> {
            // TODO: It should reject only one service logs, not all packet - find some resolution for it
            log.error(serviceLogs.getPath());
            log.error("" + serviceLogs.getServiceId());

            pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(serviceLogs.getServiceId()).orElseThrow(() -> new NotFoundException(String.format("Service %s not found. Service id not valid", serviceLogs.getServiceId().toString())));
            if(service.getAgent().getId() != monitorAgent.getId()){
                throw new ResourceNotFoundException("service","id",service.getId());
            }
            serviceLogs.getLogs().forEach(logEntry -> {
                CollectedLog collectedLog = new CollectedLog(service,serviceLogs.getPath(),logEntry.getTimestamp(),logEntry.getLog());
                collectedLogsRepository.save(collectedLog);
            });
        });
    }
}
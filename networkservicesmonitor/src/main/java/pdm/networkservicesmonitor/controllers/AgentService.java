package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.AppConsts;
import pdm.networkservicesmonitor.exceptions.AppNotImplementedException;
import pdm.networkservicesmonitor.exceptions.BadRequestException;
import pdm.networkservicesmonitor.exceptions.MethodNotAllowed;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.model.AgentSettings;
import pdm.networkservicesmonitor.model.MonitorAgent;
import pdm.networkservicesmonitor.payload.agent.AgentDataResponse;
import pdm.networkservicesmonitor.payload.agent.AgentRequest;
import pdm.networkservicesmonitor.payload.agent.AgentSettingsResponse;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateRequest;
import pdm.networkservicesmonitor.payload.client.agent.AgentResponse;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.security.jwt.JwtTokenProvider;

import javax.management.monitor.Monitor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    public MonitorAgent createAgent(AgentCreateRequest agentCreateRequest) {
        MonitorAgent agent = new MonitorAgent(
                agentCreateRequest.getName(),
                agentCreateRequest.getDescription(),
                convertOriginsToList(agentCreateRequest.getAllowedOrigins())
        );
        //AgentSettings agentSettings = new AgentSettings();
        //agentSettings.setAgent(agent);
        // agent.setSettings(agentSettings);

        return agentRepository.save(agent);
    }

    public void register(AgentRequest agentRequest, String requestIp) {
        Optional<MonitorAgent> agent = agentRepository.findById(agentRequest.getAgentId());
        if (!agent.isPresent()) {
            throw new NotFoundException("Agent not found. Agent id or encryptionKey not valid");
        }

        MonitorAgent monitorAgent = agent.get();
            /*if(Hashing.sha512().hashString(monitorAgent.getEncryptionKey().toString(), StandardCharsets.UTF_8).toString()
                    .equals(agentRequest.getVerificationHash())){
                throw new NotFoundException("Agent not found. Agent id or encryptionKey not valid");

            }*/
        if (monitorAgent.isRegistered()) {
            throw new MethodNotAllowed("Agent is already registered");
        }
        if (monitorAgent.getAllowedOrigins().isEmpty()) {
            monitorAgent.setAllowedOrigins(convertOriginsToList(requestIp));
        }
        monitorAgent.setRegistered(true);
        agentRepository.save(monitorAgent);
    }

    private String convertOriginsToString(List<String> allowedOrigins) {
        StringBuilder sb = new StringBuilder();
        allowedOrigins.stream().forEach(o -> {
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
        //TODO
        log.trace(agentId.toString());
        AgentResponse agentResponse = null;
        Optional<MonitorAgent> optionalMonitorAgent = agentRepository.findById(agentId);
        if (optionalMonitorAgent.isPresent()) {
            MonitorAgent a = optionalMonitorAgent.get();
            agentResponse = new AgentResponse(a.getId(), a.getName(), a.getDescription(), convertOriginsToString(a.getAllowedOrigins()));
        }
        /*return agentRepository.findById(agentId)
                .filter(Predicate.not(null))
                .map(a -> new AgentResponse(a.getId(), a.getName(), a.getDescription(), convertOriginsToString(a.getAllowedOrigins())))
                .get();*/
        return agentResponse;
    }

    public AgentSettingsResponse getAgentSettings(AgentRequest agentRequest, String authToken, String requestIp) {
        MonitorAgent monitorAgent = getAgent(agentRequest.getAgentId(),authToken,requestIp);
        AgentSettings settings = monitorAgent.getSettings();
        return new AgentSettingsResponse(settings.getId(),settings.getLatency(),settings.getLogFoldersToMonitor(),settings.getParametersToMonitor());
    }

    private boolean filterRequestIp(String requestIp, List<String> allowedOrigins) {
        if(allowedOrigins.contains(requestIp)){
            return true;
        }
        if(allowedOrigins.contains("*")){
            return true;
        }
        // TODO(high): check if requestIp is in allowedOrigins, some filter to check masks, partial addresses
        return false;
    }

    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConsts.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size cannot be greater than " + AppConsts.MAX_PAGE_SIZE);
        }
    }

    public boolean checkRegistrationStatus(AgentRequest agentRequest, String authToken, String requestIp) {

        MonitorAgent monitorAgent = getAgent(agentRequest.getAgentId(),authToken,requestIp);

        if (monitorAgent.isRegistered()) {
            return true;
        }
        return false;
    }

    private MonitorAgent getAgent(UUID agentId, String authToken, String requestIp){
        Optional<MonitorAgent> agent = agentRepository.findById(agentId);
        if (!agent.isPresent()) {
            throw new NotFoundException(String.format("Agent %s not found. Agent id or encryptionKey not valid", agentId.toString()));
        }
        MonitorAgent monitorAgent = agent.get();

        if (!jwtTokenProvider.validateAgentToken(authToken, monitorAgent.getEncryptionKey())) {
            throw new NotFoundException(String.format("Agent %s not found. Agent id or encryptionKey not valid", agentId.toString()));
        }

        if (!monitorAgent.getAllowedOrigins().isEmpty() && !filterRequestIp(requestIp,monitorAgent.getAllowedOrigins())) {
            throw new MethodNotAllowed("Current ip address not in allowed origins. Set appropriate allowed origins or left it blank to auto fill");
        }
        return monitorAgent;
    }


}
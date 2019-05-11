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
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateRequest;
import pdm.networkservicesmonitor.payload.client.agent.AgentResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddLogsConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddMonitoredParameterConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceCreateRequest;
import pdm.networkservicesmonitor.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.AgentServicesUtil.convertOriginsToList;
import static pdm.networkservicesmonitor.service.AgentServicesUtil.convertOriginsToString;

@Service
@Slf4j
public class AgentService {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private LogsCollectingConfigurationRepository logsCollectingConfigurationRepository;
    @Autowired
    private MonitoredParameterTypeRepository monitoredParameterTypeRepository;
    @Autowired
    private MonitoredParameterConfigurationRepository monitoredParameterConfigurationRepository;


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
                new pdm.networkservicesmonitor.model.agent.service.Service(serviceCreateRequest.getName(), serviceCreateRequest.getDescription(), agent);
        agent.addService(service);
        if (agentRepository.save(agent) != null) {
            return serviceRepository.save(service);
        }
        return null;
    }

    public LogsCollectingConfiguration addLogsCollectionConfiguration(ServiceAddLogsConfigurationRequest configurationRequest) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        LogsCollectingConfiguration logsCollectingConfiguration = new LogsCollectingConfiguration(configurationRequest.getPath(), configurationRequest.getMonitoredFilesMasks(), configurationRequest.getUnmonitoredFileMasks(), service);
        return logsCollectingConfigurationRepository.save(logsCollectingConfiguration);
    }

    public MonitoredParameterConfiguration addMonitoredParameterConfiguration(ServiceAddMonitoredParameterConfigurationRequest configurationRequest) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository.findById(configurationRequest.getParameterTypeId()).orElseThrow(() ->
                new NotFoundException(("Parameter type not found. Parameter type is not valid")));
        MonitoredParameterConfiguration monitoredParameterConfiguration = new MonitoredParameterConfiguration(monitoredParameterType, service, configurationRequest.getDescription(), configurationRequest.getMonitoringInterval());
        return monitoredParameterConfigurationRepository.save(monitoredParameterConfiguration);
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
        MonitorAgent agent = agentRepository.findById(agentId).orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Agent Id", "id", agentId));
        return new AgentResponse(agent.getId(), agent.getName(), agent.getDescription(), convertOriginsToString(agent.getAllowedOrigins()));
    }

    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size cannot be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

}
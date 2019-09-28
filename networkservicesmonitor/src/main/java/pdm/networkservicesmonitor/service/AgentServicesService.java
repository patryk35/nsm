package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.AgentConfiguration;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.*;
import pdm.networkservicesmonitor.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.validatePageNumberAndSize;

@Service
@Slf4j
public class AgentServicesService {

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

    public pdm.networkservicesmonitor.model.agent.service.Service createService(ServiceCreateRequest serviceCreateRequest) {
        MonitorAgent agent = agentRepository.findById(serviceCreateRequest.getAgentId()).orElseThrow(() ->
                new NotFoundException("Agent not found. Agent id is not valid"));
        if (agent.isDeleted()) {
            throw new NotFoundException(String.format("Agent with id %s was removed", serviceCreateRequest.getAgentId()));
        }
        pdm.networkservicesmonitor.model.agent.service.Service service =
                new pdm.networkservicesmonitor.model.agent.service.Service(serviceCreateRequest.getName(), serviceCreateRequest.getDescription(), agent);
        agent.addService(service);
        if (agentRepository.save(agent) != null) {
            return serviceRepository.save(service);
        }
        return null;
    }

    public ServiceResponse getServiceDetailsById(UUID serviceId) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Service Id", "id", serviceId));
        if (service.isDeleted()) {
            throw new NotFoundException(String.format("Service with id %s was removed", serviceId));
        }
        return new ServiceResponse(service.getId(), service.getName(), service.getDescription());

    }

    public void deleteService(UUID serviceId) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException(String.format("Service with id %s doesn't exist", serviceId)));
        if (service.isDeleted()) {
            throw new NotFoundException(String.format("Service with id %s was already removed", serviceId));
        }
        service.setDeleted(true);
        service.getLogsCollectingConfigurations().forEach(c -> c.setDeleted(true));
        service.getMonitoredParametersConfigurations().forEach(c -> c.setDeleted(true));
        serviceRepository.save(service);
    }

    public LogsCollectingConfiguration addLogsCollectionConfiguration(ServiceAddLogsConfigurationRequest configurationRequest) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        if (service.isDeleted()) {
            throw new NotFoundException(String.format("Service with id %s was removed", configurationRequest.getServiceId()));
        }
        LogsCollectingConfiguration logsCollectingConfiguration = new LogsCollectingConfiguration(configurationRequest.getPath(), configurationRequest.getMonitoredFilesMask(), configurationRequest.getLogLineRegex(), service);
        return logsCollectingConfigurationRepository.save(logsCollectingConfiguration);
    }

    public MonitoredParameterConfiguration addMonitoredParameterConfiguration(ServiceAddMonitoredParameterConfigurationRequest configurationRequest) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        if (service.isDeleted()) {
            throw new NotFoundException(String.format("Service with id %s was removed", configurationRequest.getServiceId()));
        }
        MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository.findById(configurationRequest.getParameterTypeId()).orElseThrow(() ->
                new NotFoundException(("Parameter type not found. Parameter type is not valid")));
        MonitoredParameterConfiguration monitoredParameterConfiguration = new MonitoredParameterConfiguration(monitoredParameterType, service, configurationRequest.getDescription(), configurationRequest.getMonitoringInterval());
        return monitoredParameterConfigurationRepository.save(monitoredParameterConfiguration);
    }

    public void deleteLogsCollectingConfiguration(UUID configurationId) {
        LogsCollectingConfiguration logsCollectingConfiguration = logsCollectingConfigurationRepository.findById(configurationId)
                .orElseThrow(() -> new NotFoundException(String.format("Configuration with id %s doesn't exist", configurationId)));
        if (logsCollectingConfiguration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was already removed", configurationId));
        }
        logsCollectingConfiguration.setDeleted(true);
        logsCollectingConfigurationRepository.save(logsCollectingConfiguration);
    }

    public void deleteMonitoredParameterConfiguration(UUID configurationId) {
        MonitoredParameterConfiguration configuration = monitoredParameterConfigurationRepository.findById(configurationId)
                .orElseThrow(() -> new NotFoundException(String.format("Configuration with id %s doesn't exist", configurationId)));
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was already removed", configurationId));
        }
        configuration.setDeleted(true);
        monitoredParameterConfigurationRepository.save(configuration);
    }

    public PagedResponse<ServiceMonitoringConfigurationResponse> getServiceMonitoringConfigurationDetailsById(UUID serviceId, int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<MonitoredParameterConfiguration> configurations = monitoredParameterConfigurationRepository.findByServiceIdAndIsDeleted(serviceId, false, pageable);
        if (configurations.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), configurations.getNumber(),
                    configurations.getSize(), configurations.getTotalElements(), configurations.getTotalPages(), configurations.isLast());
        }
        List<ServiceMonitoringConfigurationResponse> list = configurations.getContent().stream()
                .map(e -> new ServiceMonitoringConfigurationResponse(e.getId(), e.getParameterType().getName(), e.getDescription(), e.getMonitoringInterval()))
                .collect(Collectors.toList());

        return new PagedResponse<>(list, configurations.getNumber(),
                configurations.getSize(), configurations.getTotalElements(), configurations.getTotalPages(), configurations.isLast());
    }

    public PagedResponse<ServiceLogsConfigurationResponse> getServiceLogsConfigurationDetailsById(UUID serviceId, int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<LogsCollectingConfiguration> configurations = logsCollectingConfigurationRepository.findByServiceIdAndIsDeleted(serviceId, false, pageable);
        if (configurations.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), configurations.getNumber(),
                    configurations.getSize(), configurations.getTotalElements(), configurations.getTotalPages(), configurations.isLast());
        }
        List<ServiceLogsConfigurationResponse> list = configurations.getContent().stream()
                .map(e -> new ServiceLogsConfigurationResponse(e.getId(), e.getPath(), e.getMonitoredFilesMask(), e.getLogLineRegex()))
                .collect(Collectors.toList());

        return new PagedResponse<>(list, configurations.getNumber(),
                configurations.getSize(), configurations.getTotalElements(), configurations.getTotalPages(), configurations.isLast());
    }

    public void editService(ServiceEditRequest serviceEditRequest) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(serviceEditRequest.getServiceId())
                .orElseThrow(() -> new NotFoundException(String.format("Service with id %s doesn't exist", serviceEditRequest.getServiceId())));
        if (service.isDeleted()) {
            throw new NotFoundException(String.format("Service with id %s was removed", service.getId()));
        }
        service.setDescription(serviceEditRequest.getDescription());
        serviceRepository.save(service);
    }
}

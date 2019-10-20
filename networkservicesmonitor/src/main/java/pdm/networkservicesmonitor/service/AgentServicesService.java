package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.*;
import pdm.networkservicesmonitor.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.validatePageNumberAndSize;

@org.springframework.stereotype.Service
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

    public Service createService(ServiceCreateRequest serviceCreateRequest) {
        MonitorAgent agent = agentRepository.findById(serviceCreateRequest.getAgentId()).orElseThrow(() ->
                new NotFoundException("Agent not found. Agent id is not valid"));
        if (agent.isDeleted()) {
            throw new NotFoundException(String.format(
                    "Agent with id %s was removed",
                    serviceCreateRequest.getAgentId()
            ));
        }
        Service service =
                new Service(serviceCreateRequest.getName(), serviceCreateRequest.getDescription(), agent);
        agent.addService(service);
        agentRepository.save(agent);
        return serviceRepository.save(service);
    }

    public ServiceResponse getServiceDetailsById(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found. Verify Service Id", "id", serviceId));
        if (service.isDeleted()) {
            throw new NotFoundException(String.format("Service with id %s was removed", serviceId));
        }
        return new ServiceResponse(service.getId(), service.getName(), service.getDescription());

    }

    public void deleteService(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException(String.format("Service with id %s doesn't exist", serviceId)));
        if (service.isDeleted()) {
            throw new NotFoundException(String.format("Service with id %s was already removed", serviceId));
        }
        service.setDeleted(true);
        service.getLogsCollectingConfigurations().forEach(c -> c.setDeleted(true));
        service.getMonitoredParametersConfigurations().forEach(c -> c.setDeleted(true));
        serviceRepository.save(service);
        MonitorAgent agent = service.getAgent();
        agent.getAgentConfiguration().setUpdated(true);
        agentRepository.save(agent);
    }

    public LogsCollectingConfiguration addLogsCollectionConfiguration(
            ServiceAddLogsConfigurationRequest configurationRequest) {
        Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        if (service.isDeleted()) {
            throw new NotFoundException(String.format(
                    "Service with id %s was removed",
                    configurationRequest.getServiceId()
            ));
        }
        LogsCollectingConfiguration logsCollectingConfiguration = new LogsCollectingConfiguration(
                configurationRequest.getPath(),
                configurationRequest.getMonitoredFilesMask(),
                configurationRequest.getLogLineRegex(),
                service);
        logsCollectingConfiguration = logsCollectingConfigurationRepository.save(logsCollectingConfiguration);
        MonitorAgent agent = service.getAgent();
        agent.getAgentConfiguration().setUpdated(true);
        agentRepository.save(agent);
        return logsCollectingConfiguration;
    }

    public MonitoredParameterConfiguration addMonitoredParameterConfiguration(
            ServiceAddMonitoredParameterConfigurationRequest configurationRequest) {
        Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        if (service.isDeleted()) {
            throw new NotFoundException(
                    String.format("Service with id %s was removed", configurationRequest.getServiceId())
            );
        }
        MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository
                .findById(configurationRequest.getParameterTypeId())
                .orElseThrow(() ->
                        new NotFoundException(("Parameter type not found. Parameter type is not valid"))
                );
        MonitoredParameterConfiguration monitoredParameterConfiguration = new MonitoredParameterConfiguration(
                monitoredParameterType,
                service,
                configurationRequest.getDescription(),
                configurationRequest.getMonitoringInterval()
        );

        monitoredParameterConfiguration = monitoredParameterConfigurationRepository.save(monitoredParameterConfiguration);
        MonitorAgent agent = service.getAgent();
        agent.getAgentConfiguration().setUpdated(true);
        agentRepository.save(agent);
        return monitoredParameterConfiguration;
    }

    public void deleteLogsCollectingConfiguration(UUID configurationId) {
        LogsCollectingConfiguration logsCollectingConfiguration = logsCollectingConfigurationRepository
                .findById(configurationId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Configuration with id %s doesn't exist", configurationId))
                );
        if (logsCollectingConfiguration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was already removed", configurationId));
        }
        logsCollectingConfiguration.setDeleted(true);
        logsCollectingConfigurationRepository.save(logsCollectingConfiguration);
        MonitorAgent agent = logsCollectingConfiguration.getService().getAgent();
        agent.getAgentConfiguration().setUpdated(true);
        agentRepository.save(agent);
    }

    public void deleteMonitoredParameterConfiguration(UUID configurationId) {
        MonitoredParameterConfiguration configuration = monitoredParameterConfigurationRepository
                .findById(configurationId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Configuration with id %s doesn't exist", configurationId))
                );
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was already removed", configurationId));
        }
        configuration.setDeleted(true);
        monitoredParameterConfigurationRepository.save(configuration);
        MonitorAgent agent = configuration.getService().getAgent();
        agent.getAgentConfiguration().setUpdated(true);
        agentRepository.save(agent);
    }

    public PagedResponse<ServiceMonitoringConfigurationResponse>
    getServiceMonitoringConfigurationDetailsByServiceId(UUID serviceId, int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<MonitoredParameterConfiguration> configurations = monitoredParameterConfigurationRepository
                .findByServiceIdAndIsDeleted(serviceId, false, pageable);
        if (configurations.getNumberOfElements() == 0) {
            return new PagedResponse<>(
                    Collections.emptyList(),
                    configurations.getNumber(),
                    configurations.getSize(),
                    configurations.getTotalElements(),
                    configurations.getTotalPages(),
                    configurations.isLast()
            );
        }
        List<ServiceMonitoringConfigurationResponse> list = configurations.getContent().stream()
                .map(e -> new ServiceMonitoringConfigurationResponse(
                        e.getId(),
                        e.getParameterType().getName(),
                        e.getDescription(),
                        e.getMonitoringInterval())
                )
                .collect(Collectors.toList());

        return new PagedResponse<>(list,
                configurations.getNumber(),
                configurations.getSize(),
                configurations.getTotalElements(),
                configurations.getTotalPages(),
                configurations.isLast()
        );
    }

    public PagedResponse<ServiceLogsConfigurationResponse>
    getServiceLogsConfigurationDetailsByServiceId(UUID serviceId, int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<LogsCollectingConfiguration> configurations = logsCollectingConfigurationRepository
                .findByServiceIdAndIsDeleted(serviceId, false, pageable);
        if (configurations.getNumberOfElements() == 0) {
            return new PagedResponse<>(
                    Collections.emptyList(),
                    configurations.getNumber(),
                    configurations.getSize(),
                    configurations.getTotalElements(),
                    configurations.getTotalPages(),
                    configurations.isLast()
            );
        }
        List<ServiceLogsConfigurationResponse> list = configurations.getContent().stream()
                .map(e -> new ServiceLogsConfigurationResponse(
                        e.getId(),
                        e.getPath(),
                        e.getMonitoredFilesMask()
                        , e.getLogLineRegex())
                )
                .collect(Collectors.toList());

        return new PagedResponse<>(list,
                configurations.getNumber(),
                configurations.getSize(),
                configurations.getTotalElements(),
                configurations.getTotalPages(),
                configurations.isLast()
        );
    }

    public ServiceLogsConfigurationResponse getServiceLogsConfigurationDetailsById(UUID configurationId) {
        LogsCollectingConfiguration configuration = logsCollectingConfigurationRepository
                .findById(configurationId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Configuration with id %s doesn't exist", configurationId))
                );
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was removed", configurationId));
        }
        return new ServiceLogsConfigurationResponse(
                configuration.getId(),
                configuration.getPath(),
                configuration.getMonitoredFilesMask(),
                configuration.getLogLineRegex()
        );

    }

    public ServiceMonitoringConfigurationResponse getServiceMonitoringConfigurationDetailsById(UUID configurationId) {
        MonitoredParameterConfiguration configuration = monitoredParameterConfigurationRepository
                .findById(configurationId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Configuration with id %s doesn't exist", configurationId))
                );
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was removed", configurationId));
        }
        return new ServiceMonitoringConfigurationResponse(
                configuration.getId(),
                configuration.getParameterType().getName(),
                configuration.getDescription(),
                configuration.getMonitoringInterval()
        );

    }

    public void editService(ServiceEditRequest serviceEditRequest) {
        Service service = serviceRepository
                .findById(serviceEditRequest.getServiceId())
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format("Service with id %s doesn't exist",
                                serviceEditRequest.getServiceId()
                        ))
                );
        if (service.isDeleted()) {
            throw new NotFoundException(String.format("Service with id %s was removed", service.getId()));
        }
        service.setDescription(serviceEditRequest.getDescription());
        serviceRepository.save(service);
    }

    public void editLogsConfiguration(ServiceEditLogsConfigurationRequest configurationRequest) {
        LogsCollectingConfiguration configuration = logsCollectingConfigurationRepository
                .findById(configurationRequest.getConfigurationId())
                .orElseThrow(() ->
                        new NotFoundException(String.format(
                                "Configuration with id %s doesn't exist",
                                configurationRequest.getConfigurationId()
                        ))
                );
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format(
                    "Configuration with id %s was removed",
                    configurationRequest.getConfigurationId()
            ));
        }
        configuration.setLogLineRegex(configurationRequest.getLogLineRegex());
        configuration.setMonitoredFilesMask(configurationRequest.getMonitoredFilesMask());
        logsCollectingConfigurationRepository.save(configuration);
        MonitorAgent agent = configuration.getService().getAgent();
        agent.getAgentConfiguration().setUpdated(true);
        agentRepository.save(agent);
    }

    public void editMonitoringConfiguration(ServiceEditMonitoredParameterConfigurationRequest configurationRequest) {
        MonitoredParameterConfiguration configuration = monitoredParameterConfigurationRepository
                .findById(configurationRequest.getConfigurationId())
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Configuration with id %s doesn't exist",
                        configurationRequest.getConfigurationId()
                )));
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format(
                    "Configuration with id %s was removed",
                    configurationRequest.getConfigurationId()
            ));
        }
        configuration.setDescription(configurationRequest.getDescription());
        configuration.setMonitoringInterval(configurationRequest.getMonitoringInterval());
        monitoredParameterConfigurationRepository.save(configuration);
        MonitorAgent agent = configuration.getService().getAgent();
        agent.getAgentConfiguration().setUpdated(true);
        agentRepository.save(agent);
    }

    public List<ParameterTypeResponse> getAvailableParameters(UUID serviceID) {
        Service service = serviceRepository.findById(serviceID)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Service with id %s doesn't exist",
                        serviceID
                )));
        List<MonitoredParameterConfiguration> usedParametersTypes = monitoredParameterConfigurationRepository
                .findByServiceAndIsDeleted(service, false);
        if (usedParametersTypes.isEmpty()) {
            return monitoredParameterTypeRepository.findAll().stream()
                    .map(e -> new ParameterTypeResponse(
                            e.getId(),
                            e.getName(),
                            e.getDescription()
                    ))
                    .collect(Collectors.toList());
        } else {
            return monitoredParameterTypeRepository.findAll().stream()
                    .map(e -> new ParameterTypeResponse(
                            e.getId(),
                            e.getName(),
                            e.getDescription()
                    ))
                    .filter(e -> (usedParametersTypes.stream()
                            .noneMatch(u -> u.getParameterType().getId().equals(e.getId())))
                    )
                    .collect(Collectors.toList());
        }
    }

    public List<ParameterTypeResponse> getAddedParameters(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Service with id %s doesn't exist",
                        serviceId
                )));

        return monitoredParameterConfigurationRepository.findByServiceAndIsDeleted(service, false)
                .stream()
                .map(u -> u.getParameterType())
                .map(type -> new ParameterTypeResponse(type.getId(), type.getName(), type.getDescription()))
                .collect(Collectors.toList());
    }
}

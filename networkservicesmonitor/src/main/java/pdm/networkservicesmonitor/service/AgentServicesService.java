package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pdm.networkservicesmonitor.config.AppConstants;
import pdm.networkservicesmonitor.exceptions.BadRequestException;
import pdm.networkservicesmonitor.exceptions.ItemExists;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.service.Service;
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
        if (serviceRepository.findByAgentIdAndNameAndIsDeleted(agent.getId(), serviceCreateRequest.getName(), false).isPresent()) {
            throw new ItemExists(String.format("Service with name `%s` exists! Aborting.", serviceCreateRequest.getName()));
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
        return new ServiceResponse(service.getId(), service.getName(), service.getDescription(), service.isSystemService());

    }

    public void deleteService(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException(String.format("Service with id %s doesn't exist", serviceId)));
        if (service.isDeleted()) {
            throw new NotFoundException(String.format("Service with id %s was already removed", serviceId));
        }
        if (service.isSystemService()) {
            throw new BadRequestException("Deleting system service is not allowed.");
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

        // TODO(high): It is a workaround. Think how to do it better - probably chane param_id to config_id will be required in parameters DB
        if(monitoredParameterType.isRequireTargetObject()){
            monitoredParameterType = new MonitoredParameterType(
                    monitoredParameterType.getId(),
                    String.format("%s(%s)", monitoredParameterType.getName(), configurationRequest.getTargetObject()),
                    monitoredParameterType.getDescription(),
                    monitoredParameterType.getType(),
                    monitoredParameterType.isSystemParameter(),
                    monitoredParameterType.isRequireTargetObject(),
                    monitoredParameterType.getTargetObjectName()
            );
            monitoredParameterTypeRepository.save(monitoredParameterType);
        }


        if (service.isSystemService() != monitoredParameterType.isSystemParameter()) {
            throw new BadRequestException(String.format("Parameter with id %s cannot be added to this service",
                    monitoredParameterType.getId()));
        }

        MonitoredParameterConfiguration monitoredParameterConfiguration;


        if(monitoredParameterType.isRequireTargetObject()){
            monitoredParameterConfiguration = new MonitoredParameterConfiguration(
                    monitoredParameterType,
                    service,
                    configurationRequest.getDescription(),
                    configurationRequest.getMonitoringInterval(),
                    configurationRequest.getTargetObject()
            );
        } else {
            monitoredParameterConfiguration = new MonitoredParameterConfiguration(
                    monitoredParameterType,
                    service,
                    configurationRequest.getDescription(),
                    configurationRequest.getMonitoringInterval()
            );
        }

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

        MonitoredParameterType parameterType = monitoredParameterTypeRepository
                .findById(configuration.getParameterType().getId())
                .orElseThrow(() ->
                        new NotFoundException(("Parameter type not found. Parameter type is not valid"))
                );
        if(parameterType.isRequireTargetObject()){
            configuration.setTargetObject(configurationRequest.getTargetObject());
        }

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
            return monitoredParameterTypeRepository.findAllByParentId(null).stream()
                    .filter(e -> e.isSystemParameter() == service.isSystemService())
                    .map(e -> new ParameterTypeResponse(
                            e.getId(),
                            e.getName(),
                            e.getDescription(),
                            e.getTargetObjectName()
                    ))
                    .collect(Collectors.toList());
        } else {
            return monitoredParameterTypeRepository.findAllByParentId(null).stream()
                    .filter(e -> e.isSystemParameter() == service.isSystemService())
                    .filter(e -> (usedParametersTypes.stream()
                            .noneMatch(u -> u.getParameterType().getId().equals(e.getId())))
                    )
                    .map(e -> new ParameterTypeResponse(
                            e.getId(),
                            e.getName(),
                            e.getDescription(),
                            e.getTargetObjectName()
                    ))
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
                .map(MonitoredParameterConfiguration::getParameterType)
                .map(type -> new ParameterTypeResponse(type.getId(), type.getName(), type.getDescription(), type.getTargetObjectName()))
                .collect(Collectors.toList());
    }

    public Boolean checkServiceNameAvailability(String name, UUID agentId) {
        return !serviceRepository.existsByNameAndAgentIdAndIsDeleted(name, agentId, false);
    }
}

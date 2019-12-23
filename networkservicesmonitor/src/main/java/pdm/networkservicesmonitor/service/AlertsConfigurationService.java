package pdm.networkservicesmonitor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pdm.networkservicesmonitor.config.AppConstants;
import pdm.networkservicesmonitor.exceptions.BadRequestException;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.alert.AlertLevel;
import pdm.networkservicesmonitor.model.alert.LogsAlertConfiguration;
import pdm.networkservicesmonitor.model.alert.MonitoringAlertConfiguration;
import pdm.networkservicesmonitor.model.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.service.Service;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.alerts.*;
import pdm.networkservicesmonitor.repository.LogsAlertsConfigurationRepository;
import pdm.networkservicesmonitor.repository.MonitoredParameterTypeRepository;
import pdm.networkservicesmonitor.repository.MonitoringAlertsConfigurationRepository;
import pdm.networkservicesmonitor.repository.ServiceRepository;

import java.util.*;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.*;

@org.springframework.stereotype.Service
public class AlertsConfigurationService {
    Set<String> allowedConditions = new HashSet<>(Arrays.asList("<", "<=", "=", "!=", ">", ">="));
    @Autowired
    private LogsAlertsConfigurationRepository logsAlertsConfigurationRepository;
    @Autowired
    private MonitoringAlertsConfigurationRepository monitoringAlertsConfigurationRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private MonitoredParameterTypeRepository monitoredParameterTypeRepository;

    public LogsAlertConfiguration createLogsAlert(LogsAlertConfigurationCreateRequest request) {
        Service service = serviceRepository.findById(request.getServiceId()).orElseThrow(() ->
                new ResourceNotFoundException("service", "id", request.getServiceId())
        );
        if (service.isDeleted()) {
            throw new NotFoundException("Service was deleted. Cannot add alert configuration");
        }
        AlertLevel alertLevel = null;
        try {
            alertLevel = AlertLevel.valueOf(request.getAlertLevel());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Alert Level %s not found", request.getAlertLevel()));
        }

        LogsAlertConfiguration configuration = new LogsAlertConfiguration(
                service,
                request.getMessage(),
                request.getPathSearchString(),
                request.getSearchString(),
                alertLevel,
                request.isEmailNotification(),
                convertStringToList(request.getRecipients(), ";")
        );
        return logsAlertsConfigurationRepository.save(configuration);
    }

    public MonitoringAlertConfiguration createMonitoringAlert(MonitoringAlertConfigurationCreateRequest request) {
        Service service = serviceRepository.findById(request.getServiceId()).orElseThrow(() ->
                new ResourceNotFoundException("service", "id", request.getServiceId())
        );
        if (service.isDeleted()) {
            throw new NotFoundException("Service was deleted. Cannot add alert configuration");
        }
        if (!allowedConditions.contains(request.getCondition())) {
            throw new BadRequestException(String.format(
                    "Not allowed condition. You can use only %s",
                    String.join(", ", allowedConditions)
            ));
        }

        AlertLevel alertLevel = null;
        try {
            alertLevel = AlertLevel.valueOf(request.getAlertLevel());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Alert Level %s not found", request.getAlertLevel()));
        }

        MonitoredParameterType parameterType = monitoredParameterTypeRepository
                .findById(request.getMonitoredParameterTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Monitoring parameter type", "id",
                        request.getMonitoredParameterTypeId())
                );

        MonitoringAlertConfiguration configuration = new MonitoringAlertConfiguration(
                service,
                parameterType,
                request.getMessage(),
                request.getCondition(),
                request.getValue(),
                alertLevel,
                request.isEmailNotification(),
                convertStringToList(request.getRecipients(), ";")
        );
        return monitoringAlertsConfigurationRepository.save(configuration);
    }

    public PagedResponse<LogsAlertConfigurationDetailsResponse> getAllLogsAlertsConfigurations(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "serviceId");
        Page<LogsAlertConfiguration> configurations = logsAlertsConfigurationRepository
                .findByDeleted(false, pageable);
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
        List<LogsAlertConfigurationDetailsResponse> list = configurations.getContent().stream()
                .map(e -> new LogsAlertConfigurationDetailsResponse(
                        e.getId(),
                        e.getService().getId(),
                        e.getService().getName(),
                        e.getService().getAgent().getId(),
                        e.getService().getAgent().getName(),
                        e.getMessage(),
                        e.getPathSearchString(),
                        e.getSearchString(),
                        e.isEnabled(),
                        e.isDeleted(),
                        e.getAlertLevel(),
                        e.isEmailNotification(),
                        convertListToString(e.getRecipients(), ";")
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(list,
                configurations.getNumber(),
                configurations.getSize(),
                configurations.getTotalElements(),
                configurations.getTotalPages(),
                configurations.isLast()
        );
    }

    public PagedResponse<MonitoringAlertConfigurationDetailsResponse> getAllMonitoringAlertsConfigurations(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "serviceId");
        Page<MonitoringAlertConfiguration> configurations = monitoringAlertsConfigurationRepository
                .findByDeleted(false, pageable);
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
        List<MonitoringAlertConfigurationDetailsResponse> list = configurations.getContent().stream()
                .map(e -> new MonitoringAlertConfigurationDetailsResponse(
                        e.getId(),
                        e.getService().getId(),
                        e.getService().getName(),
                        e.getService().getAgent().getId(),
                        e.getService().getAgent().getName(),
                        e.getMonitoredParameterType().getId(),
                        e.getMonitoredParameterType().getName(),
                        e.getMessage(),
                        e.getCondition(),
                        e.getValue(),
                        e.isEnabled(),
                        e.isDeleted(),
                        e.getAlertLevel(),
                        e.isEmailNotification(),
                        convertListToString(e.getRecipients(), ";")
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(list,
                configurations.getNumber(),
                configurations.getSize(),
                configurations.getTotalElements(),
                configurations.getTotalPages(),
                configurations.isLast()
        );
    }

    public void editLogsAlertConfiguration(LogsAlertConfigurationEditRequest request) {
        LogsAlertConfiguration configuration = logsAlertsConfigurationRepository.findById(request.getAlertId())
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Configuration with id %s doesn't exist",
                        request.getAlertId()))
                );
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format(
                    "Configuration with id %s was already removed",
                    request.getAlertId())
            );
        }

        AlertLevel alertLevel = null;
        try {
            alertLevel = AlertLevel.valueOf(request.getAlertLevel());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Alert Level %s not found", request.getAlertLevel()));
        }

        configuration.setMessage(request.getMessage());
        configuration.setPathSearchString(request.getPathSearchString());
        configuration.setSearchString(request.getSearchString());
        configuration.setEnabled(request.isEnabled());
        configuration.setAlertLevel(alertLevel);
        configuration.setEmailNotification(request.isEmailNotification());
        configuration.setRecipients(convertStringToList(request.getRecipients(), ";"));
        logsAlertsConfigurationRepository.save(configuration);
    }

    public void editMonitoringAlertConfiguration(MonitoringAlertConfigurationEditRequest request) {
        MonitoringAlertConfiguration configuration = monitoringAlertsConfigurationRepository.findById(request.getAlertId())
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Configuration with id %s doesn't exist",
                        request.getAlertId()))
                );
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format(
                    "Configuration with id %s was already removed",
                    request.getAlertId())
            );
        }
        if (!allowedConditions.contains(request.getCondition())) {
            throw new BadRequestException(String.format(
                    "Not allowed condition. You can use only %s",
                    String.join(", ", allowedConditions)
            ));
        }

        AlertLevel alertLevel = null;
        try {
            alertLevel = AlertLevel.valueOf(request.getAlertLevel());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Alert Level %s not found", request.getAlertLevel()));
        }

        configuration.setMessage(request.getMessage());
        configuration.setCondition(request.getCondition());
        configuration.setValue(request.getValue());
        configuration.setEnabled(request.isEnabled());
        configuration.setAlertLevel(alertLevel);
        configuration.setEmailNotification(request.isEmailNotification());
        configuration.setRecipients(convertStringToList(request.getRecipients(), ";"));
        monitoringAlertsConfigurationRepository.save(configuration);
    }

    public MonitoringAlertConfigurationDetailsResponse getMonitoringAlertConfigurationDetails(UUID id) {
        MonitoringAlertConfiguration configuration = monitoringAlertsConfigurationRepository
                .findByIdAndDeleted(id, false)
                .orElseThrow(() -> new ResourceNotFoundException("Not found. Verify configuration Id", "id", id));
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was removed", id));
        }
        return new MonitoringAlertConfigurationDetailsResponse(
                configuration.getId(),
                configuration.getService().getId(),
                configuration.getService().getName(),
                configuration.getService().getAgent().getId(),
                configuration.getService().getAgent().getName(),
                configuration.getMonitoredParameterType().getId(),
                configuration.getMonitoredParameterType().getName(),
                configuration.getMessage(),
                configuration.getCondition(),
                configuration.getValue(),
                configuration.isEnabled(),
                configuration.isDeleted(),
                configuration.getAlertLevel(),
                configuration.isEmailNotification(),
                convertListToString(configuration.getRecipients(), ";")
        );
    }

    public LogsAlertConfigurationDetailsResponse getLogsAlertConfigurationDetails(UUID id) {
        LogsAlertConfiguration configuration = logsAlertsConfigurationRepository
                .findByIdAndDeleted(id, false)
                .orElseThrow(() -> new ResourceNotFoundException("Not found. Verify configuration Id", "id", id));
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was removed", id));
        }
        return new LogsAlertConfigurationDetailsResponse(
                configuration.getId(),
                configuration.getService().getId(),
                configuration.getService().getName(),
                configuration.getService().getAgent().getId(),
                configuration.getService().getAgent().getName(),
                configuration.getMessage(),
                configuration.getPathSearchString(),
                configuration.getSearchString(),
                configuration.isEnabled(),
                configuration.isDeleted(),
                configuration.getAlertLevel(),
                configuration.isEmailNotification(),
                convertListToString(configuration.getRecipients(), ";")
        );
    }

    public void deleteLogsAlertConfiguration(UUID id) {
        LogsAlertConfiguration configuration = logsAlertsConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Configuration with id %s doesn't exist", id)));
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was already removed", id));
        }
        configuration.setDeleted(true);
        logsAlertsConfigurationRepository.save(configuration);
    }

    public void deleteMonitoringAlertConfiguration(UUID id) {
        MonitoringAlertConfiguration configuration = monitoringAlertsConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Configuration with id %s doesn't exist", id)));
        if (configuration.isDeleted()) {
            throw new NotFoundException(String.format("Configuration with id %s was already removed", id));
        }
        configuration.setDeleted(true);
        monitoringAlertsConfigurationRepository.save(configuration);
    }
}

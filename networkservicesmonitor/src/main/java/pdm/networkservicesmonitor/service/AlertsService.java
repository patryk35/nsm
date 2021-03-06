package pdm.networkservicesmonitor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pdm.networkservicesmonitor.config.AppConstants;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.model.data.AgentError;
import pdm.networkservicesmonitor.model.data.LogsAlert;
import pdm.networkservicesmonitor.model.data.MonitoringAlert;
import pdm.networkservicesmonitor.model.data.UserAlert;
import pdm.networkservicesmonitor.model.user.User;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.alerts.*;
import pdm.networkservicesmonitor.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.validatePageNumberAndSize;

@org.springframework.stereotype.Service
public class AlertsService {
    @Autowired
    private LogsAlertsRepository logsAlertsRepository;
    @Autowired
    private MonitoringAlertsRepository monitoringAlertsRepository;
    @Autowired
    private UserAlertsRepository userAlertsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AgentErrorRepository agentErrorRepository;

    public PagedResponse<LogsAlertResponse> getLogsAlerts(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<LogsAlert> alerts = logsAlertsRepository.findAll(pageable);
        if (alerts.getNumberOfElements() == 0) {
            return new PagedResponse<>(
                    Collections.emptyList(),
                    alerts.getNumber(),
                    alerts.getSize(),
                    alerts.getTotalElements(),
                    alerts.getTotalPages(),
                    alerts.isLast());
        }
        List<LogsAlertResponse> list = alerts.getContent().stream()
                .map(e -> new LogsAlertResponse(
                        e.getId(),
                        e.getLog().getService().getAgent().getName(),
                        e.getLog().getService().getName(),
                        e.getLog().getTimestamp(),
                        e.getConfiguration().getMessage(),
                        e.getConfiguration().getAlertLevel()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                list,
                alerts.getNumber(),
                alerts.getSize(),
                alerts.getTotalElements(),
                alerts.getTotalPages(),
                alerts.isLast()
        );
    }

    public LogsAlertDetailsResponse getLogsAlert(Long id) {
        LogsAlert logsAlert = logsAlertsRepository
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Log alert for provided id not found")
                );
        return new LogsAlertDetailsResponse(
                logsAlert.getId(),
                logsAlert.getLog().getService().getAgent().getName(),
                logsAlert.getLog().getService().getName(),
                logsAlert.getLog().getTimestamp(),
                logsAlert.getConfiguration().getMessage(),
                logsAlert.getConfiguration().getPathSearchString(),
                logsAlert.getConfiguration().getSearchString(),
                logsAlert.getLog().getLog(),
                logsAlert.getConfiguration().getAlertLevel()
        );
    }

    public PagedResponse<MonitoringAlertResponse> getMonitoringAlerts(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<MonitoringAlert> alerts = monitoringAlertsRepository.findAll(pageable);
        if (alerts.getNumberOfElements() == 0) {
            return new PagedResponse<>(
                    Collections.emptyList(),
                    alerts.getNumber(),
                    alerts.getSize(),
                    alerts.getTotalElements(),
                    alerts.getTotalPages(),
                    alerts.isLast());
        }
        List<MonitoringAlertResponse> list = alerts.getContent().stream()
                .map(e -> new MonitoringAlertResponse(
                        e.getId(),
                        e.getValue().getService().getAgent().getName(),
                        e.getValue().getService().getName(),
                        e.getValue().getParameterType().getName(),
                        e.getValue().getTimestamp(),
                        e.getConfiguration().getMessage(),
                        e.getConfiguration().getAlertLevel()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                list,
                alerts.getNumber(),
                alerts.getSize(),
                alerts.getTotalElements(),
                alerts.getTotalPages(),
                alerts.isLast()
        );
    }

    public MonitoringAlertDetailsResponse getMonitoringAlert(Long id) {
        MonitoringAlert monitoringAlert = monitoringAlertsRepository
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Monitoring alert for provided id not found")
                );
        double multiplier = monitoringAlert.getConfiguration().getMonitoredParameterType().getUnit().equals("%") ? 100 :
                monitoringAlert.getConfiguration().getMonitoredParameterType().getMultiplier();
        return new MonitoringAlertDetailsResponse(
                monitoringAlert.getId(),
                monitoringAlert.getValue().getService().getAgent().getName(),
                monitoringAlert.getValue().getService().getName(),
                monitoringAlert.getValue().getParameterType().getName(),
                monitoringAlert.getValue().getTimestamp(),
                monitoringAlert.getConfiguration().getMessage(),
                monitoringAlert.getConfiguration().getCondition(),
                String.format("%f %s", monitoringAlert.getConfiguration().getValue(), monitoringAlert.getConfiguration().getMonitoredParameterType().getUnit()),
                String.format("%f %s", monitoringAlert.getValue().getValue() * multiplier, monitoringAlert.getConfiguration().getMonitoredParameterType().getUnit()),
                monitoringAlert.getConfiguration().getAlertLevel()
        );
    }

    public PagedResponse<UserAlert> getUsersAlerts(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<UserAlert> alerts = userAlertsRepository.findAll(pageable);
        return new PagedResponse<>(
                alerts.getContent(),
                alerts.getNumber(),
                alerts.getSize(),
                alerts.getTotalElements(),
                alerts.getTotalPages(),
                alerts.isLast()
        );
    }

    public UserAlertDetails getUsersAlert(Long id) {
        UserAlert userAlert = userAlertsRepository
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundException("User alert for provided id not found")
                );
        User user = userAlert.getUser();
        return new UserAlertDetails(
                userAlert.getId(),
                user.getId(),
                user.getEmail(),
                user.getFullname(),
                user.getUsername(),
                userAlert.getMessage(),
                userAlert.getTimestamp(),
                userAlert.getAlertLevel()
        );
    }

    public PagedResponse<AgentError> getAgentsErrors(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<AgentError> errors = agentErrorRepository.findAll(pageable);
        return new PagedResponse<>(
                errors.getContent(),
                errors.getNumber(),
                errors.getSize(),
                errors.getTotalElements(),
                errors.getTotalPages(),
                errors.isLast()
        );
    }
}

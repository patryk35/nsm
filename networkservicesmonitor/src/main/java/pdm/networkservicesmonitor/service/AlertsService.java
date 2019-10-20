package pdm.networkservicesmonitor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.model.data.LogsAlert;
import pdm.networkservicesmonitor.model.data.MonitoringAlert;
import pdm.networkservicesmonitor.model.data.UserAlert;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.alerts.LogsAlertResponse;
import pdm.networkservicesmonitor.payload.client.alerts.MonitoringAlertResponse;
import pdm.networkservicesmonitor.repository.LogsAlertsRepository;
import pdm.networkservicesmonitor.repository.MonitoringAlertsRepository;
import pdm.networkservicesmonitor.repository.UserAlertsRepository;

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

    public PagedResponse<LogsAlertResponse> getLogsAlertsConfiguration(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<LogsAlert> agents = logsAlertsRepository.findAll(pageable);
        if (agents.getNumberOfElements() == 0) {
            return new PagedResponse<>(
                    Collections.emptyList(),
                    agents.getNumber(),
                    agents.getSize(),
                    agents.getTotalElements(),
                    agents.getTotalPages(),
                    agents.isLast());
        }
        List<LogsAlertResponse> list = agents.getContent().stream()
                .map(e -> new LogsAlertResponse(
                        e.getId(),
                        e.getLog().getService().getAgent().getName(),
                        e.getLog().getService().getName(),
                        e.getLog().getTimestamp(),
                        e.getConfiguration().getMessage()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                list,
                agents.getNumber(),
                agents.getSize(),
                agents.getTotalElements(),
                agents.getTotalPages(),
                agents.isLast()
        );
    }

    public PagedResponse<MonitoringAlertResponse> getMonitoringAlertsConfiguration(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<MonitoringAlert> agents = monitoringAlertsRepository.findAll(pageable);
        if (agents.getNumberOfElements() == 0) {
            return new PagedResponse<>(
                    Collections.emptyList(),
                    agents.getNumber(),
                    agents.getSize(),
                    agents.getTotalElements(),
                    agents.getTotalPages(),
                    agents.isLast());
        }
        List<MonitoringAlertResponse> list = agents.getContent().stream()
                .map(e -> new MonitoringAlertResponse(
                        e.getId(),
                        e.getValue().getService().getAgent().getName(),
                        e.getValue().getService().getName(),
                        e.getValue().getParameterType().getName(),
                        e.getValue().getTimestamp(),
                        e.getConfiguration().getMessage()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                list,
                agents.getNumber(),
                agents.getSize(),
                agents.getTotalElements(),
                agents.getTotalPages(),
                agents.isLast()
        );
    }

    public PagedResponse<UserAlert> getUserMonitoringConfiguration(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<UserAlert> agents = userAlertsRepository.findAll(pageable);
        return new PagedResponse<>(
                agents.getContent(),
                agents.getNumber(),
                agents.getSize(),
                agents.getTotalElements(),
                agents.getTotalPages(),
                agents.isLast()
        );
    }
}

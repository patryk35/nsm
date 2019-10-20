package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.model.data.UserAlert;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.alerts.LogsAlertResponse;
import pdm.networkservicesmonitor.payload.client.alerts.MonitoringAlertResponse;
import pdm.networkservicesmonitor.service.AlertsService;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/alerts")
public class AlertsController {
    @Autowired
    private AlertsService alertsService;

    @GetMapping("/logs")
    public PagedResponse<LogsAlertResponse> getLogsAlertsConfiguration(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return alertsService.getLogsAlertsConfiguration(page,size);
    }

    @GetMapping("/monitoring")
    public PagedResponse<MonitoringAlertResponse> getMonitoringAlertsConfiguration(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return alertsService.getMonitoringAlertsConfiguration(page,size);
    }

    @GetMapping("/user")
    public PagedResponse<UserAlert> getUserAlertsConfiguration(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return alertsService.getUserMonitoringConfiguration(page,size);
    }
}

package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.model.data.UserAlert;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.alerts.*;
import pdm.networkservicesmonitor.service.AlertsService;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/alerts")
public class AlertsController {
    @Autowired
    private AlertsService alertsService;

    @GetMapping("/logs")
    public PagedResponse<LogsAlertResponse> getLogsAlerts(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return alertsService.getLogsAlerts(page,size);
    }

    @GetMapping("/logs/{id}")
    public LogsAlertDetailsResponse getLogsAlert(@PathVariable Long id) {
        return alertsService.getLogsAlert(id);
    }

    @GetMapping("/monitoring")
    public PagedResponse<MonitoringAlertResponse> getMonitoringAlerts(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return alertsService.getMonitoringAlerts(page,size);
    }

    @GetMapping("/monitoring/{id}")
    public MonitoringAlertDetailsResponse getMonitoringAlert(@PathVariable Long id) {
        return alertsService.getMonitoringAlert(id);
    }


    @GetMapping("/user")
    public PagedResponse<UserAlert> getUserAlerts(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return alertsService.getUsersAlerts(page,size);
    }

    @GetMapping("/user/{id}")
    public UserAlertDetails getUserAlert(@PathVariable Long id) {
        return alertsService.getUsersAlert(id);
    }
}

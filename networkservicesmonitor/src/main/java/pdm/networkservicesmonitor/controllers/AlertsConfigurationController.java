package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pdm.networkservicesmonitor.config.AppConstants;
import pdm.networkservicesmonitor.model.alert.LogsAlertConfiguration;
import pdm.networkservicesmonitor.model.alert.MonitoringAlertConfiguration;
import pdm.networkservicesmonitor.payload.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.CreateResponse;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.alerts.*;
import pdm.networkservicesmonitor.service.AlertsConfigurationService;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/alerts/config")
public class AlertsConfigurationController {
    @Autowired
    private AlertsConfigurationService alertsConfigurationService;

    @GetMapping("/logs")
    public PagedResponse<LogsAlertConfigurationDetailsResponse> getLogsAlertsConfiguration(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return alertsConfigurationService.getAllLogsAlertsConfigurations(page, size);
    }

    @GetMapping("/monitoring")
    public PagedResponse<MonitoringAlertConfigurationDetailsResponse> getMonitoringAlertsConfiguration(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return alertsConfigurationService.getAllMonitoringAlertsConfigurations(page, size);
    }

    @PostMapping("/logs")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> createLogsAlert(@Valid @RequestBody LogsAlertConfigurationCreateRequest request) {
        LogsAlertConfiguration configuration = alertsConfigurationService.createLogsAlert(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/logs/{id}")
                .buildAndExpand(configuration.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Alert configuration created Successfully", HttpStatus.OK, configuration.getId()));
    }

    @PostMapping("/monitoring")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> createMonitoringAlert(@Valid @RequestBody MonitoringAlertConfigurationCreateRequest request) {
        MonitoringAlertConfiguration configuration = alertsConfigurationService.createMonitoringAlert(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/monitoring/{id}")
                .buildAndExpand(configuration.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Alert configuration created Successfully", HttpStatus.OK, configuration.getId()));
    }

    @PatchMapping("/logs")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> editLogsAlertConfiguration(@Valid @RequestBody LogsAlertConfigurationEditRequest request) {
        alertsConfigurationService.editLogsAlertConfiguration(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(request.getAlertId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Alert configuration edited successfully", HttpStatus.OK));
    }

    @PatchMapping("/monitoring")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> editMonitoringAlertConfiguration(@Valid @RequestBody MonitoringAlertConfigurationEditRequest request) {
        alertsConfigurationService.editMonitoringAlertConfiguration(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(request.getAlertId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Alert configuration edited successfully", HttpStatus.OK));
    }


    @GetMapping("/logs/{id}")
    public LogsAlertConfigurationDetailsResponse getLogsAlertConfiguration(@PathVariable UUID id) {
        return alertsConfigurationService.getLogsAlertConfigurationDetails(id);
    }

    @GetMapping("/monitoring/{id}")
    public MonitoringAlertConfigurationDetailsResponse getMonitoringAlertConfiguration(@PathVariable UUID id) {
        return alertsConfigurationService.getMonitoringAlertConfigurationDetails(id);
    }

    @DeleteMapping("/logs/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> deleteLogsAlertConfiguration(@PathVariable UUID id) {
        alertsConfigurationService.deleteLogsAlertConfiguration(id);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/").build().toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Configuration deleted successfully", HttpStatus.OK));
    }

    @DeleteMapping("/monitoring/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> deleteMonitoringAlertConfiguration(@PathVariable UUID id) {
        alertsConfigurationService.deleteMonitoringAlertConfiguration(id);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/").build().toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Configuration deleted successfully", HttpStatus.OK));
    }
}

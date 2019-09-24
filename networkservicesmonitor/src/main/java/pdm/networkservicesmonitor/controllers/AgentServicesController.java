package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.payload.client.CreateResponse;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddLogsConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddMonitoredParameterConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceCreateRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceResponse;
import pdm.networkservicesmonitor.service.AgentService;
import pdm.networkservicesmonitor.service.AgentServicesService;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/agent/service")
public class AgentServicesController {

    @Autowired
    private AgentServicesService agentServicesService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> createService(@Valid @RequestBody ServiceCreateRequest serviceCreateRequest) {
        Service service = agentServicesService.createService(serviceCreateRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{serviceId}")
                .buildAndExpand(service.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Service Created Successfully", HttpStatus.OK, service.getId()));
    }

    @PostMapping("/logConfig")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> addLogsConfiguration(@Valid @RequestBody ServiceAddLogsConfigurationRequest serviceAddLogsConfigurationRequest) {
        LogsCollectingConfiguration configuration = agentServicesService.addLogsCollectionConfiguration(serviceAddLogsConfigurationRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{logConfigurationId}")
                .buildAndExpand(configuration.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Configuration Added Successfully", HttpStatus.OK, configuration.getId()));
    }


    @PostMapping("/parameterConfig")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> addMonitoredParameterConfiguration(@Valid @RequestBody ServiceAddMonitoredParameterConfigurationRequest serviceAddMonitoredParameterConfigurationRequest) {
        MonitoredParameterConfiguration configuration = agentServicesService.addMonitoredParameterConfiguration(serviceAddMonitoredParameterConfigurationRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{monitoredParameterConfiguration}")
                .buildAndExpand(configuration.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Configuration Added Successfully", HttpStatus.OK, configuration.getId()));
    }
}

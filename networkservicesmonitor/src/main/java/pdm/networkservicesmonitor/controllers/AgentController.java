package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.payload.client.*;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateRequest;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddLogsConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddMonitoredParameterConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceCreateRequest;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.service.AgentService;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/agent")
public class AgentController {

    @Autowired
    private AgentRepository agentRepository;


    @Autowired
    private AgentService agentService;

    /* TODO(minor): end it during implementing user profile
    @GetMapping
    public PagedResponse<AgentResponse> getAgentsCreatedByCurrentUser(@AuthenticationPrincipal UserSecurityDetails currentUser,
                                                  @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                  @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return agentService.getAllAgents(currentUser, page, size);
    }*/

    // TODO(major): Fix this during implementing usage in front
    @GetMapping
    public PagedResponse<AgentResponse> getAgents(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return agentService.getAllAgents(page, size);
    }

    //TODO(major): move it to other controller
    @PostMapping("/logs")
    public PagedResponse<LogValue> getLogEntries(@Valid @RequestBody LogsRequest logsRequest) {
        return agentService.getLogsByQuery(logsRequest);
    }

    //TODO(major): move it to other controller
    @PostMapping("/monitoring")
    public List<MonitoredParameterValuesResponse> getMonitoredParameters(@Valid @RequestBody MonitoredParameterRequest monitoredParameterRequest) {
        return agentService.getMonitoringByQuery(monitoredParameterRequest);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> createAgent(@Valid @RequestBody AgentCreateRequest agentCreateRequest) {
        MonitorAgent agent = agentService.createAgent(agentCreateRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{agentId}")
                .buildAndExpand(agent.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new AgentCreateResponse(true, "Agent Created Successfully", HttpStatus.OK, agent.getId(), agent.getEncryptionKey()));
    }

    @PostMapping("/service")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> createService(@Valid @RequestBody ServiceCreateRequest serviceCreateRequest) {
        Service service = agentService.createService(serviceCreateRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{serviceId}")
                .buildAndExpand(service.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Service Created Successfully", HttpStatus.OK, service.getId()));
    }

    @PostMapping("/service/logConfig")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> addLogsConfiguration(@Valid @RequestBody ServiceAddLogsConfigurationRequest serviceAddLogsConfigurationRequest) {
        LogsCollectingConfiguration configuration = agentService.addLogsCollectionConfiguration(serviceAddLogsConfigurationRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{logConfigurationId}")
                .buildAndExpand(configuration.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Configuration Added Successfully", HttpStatus.OK, configuration.getId()));
    }


    @PostMapping("/service/parameterConfig")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> addMonitoredParameterConfiguration(@Valid @RequestBody ServiceAddMonitoredParameterConfigurationRequest serviceAddMonitoredParameterConfigurationRequest) {
        MonitoredParameterConfiguration configuration = agentService.addMonitoredParameterConfiguration(serviceAddMonitoredParameterConfigurationRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{monitoredParameterConfiguration}")
                .buildAndExpand(configuration.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Configuration Added Successfully", HttpStatus.OK, configuration.getId()));
    }

    @GetMapping("/{agentId}")
    public AgentResponse getAgentById(@PathVariable UUID agentId) {
        log.trace(agentRepository.findById(agentId).get().getName());
        return agentService.getAgentById(agentId);
    }


}

package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.payload.client.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.CreateResponse;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateRequest;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentResponse;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddLogsConfiguration;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceCreateRequest;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.security.UserSecurityDetails;
import pdm.networkservicesmonitor.service.AgentService;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/agent")
public class AgentController {

    @Autowired
    private AgentRepository agentRepository;


    @Autowired
    private AgentService agentService;

    /* TODO(medium): end it during implementing user profile
    @GetMapping
    public PagedResponse<AgentResponse> getAgentsCreatedByCurrentUser(@AuthenticationPrincipal UserSecurityDetails currentUser,
                                                  @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                  @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return agentService.getAllAgents(currentUser, page, size);
    }*/

    @GetMapping
    public PagedResponse<AgentResponse> getAgents(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return agentService.getAllAgents(page, size);
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
                .fromCurrentRequest().path("/{agentId}")
                .buildAndExpand(service.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Service Created Successfully", HttpStatus.OK,service.getId()));
    }

    @PostMapping("/service/logConfig")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> addLogsConfiguration(@Valid @RequestBody ServiceAddLogsConfiguration serviceAddLogsConfiguration) {
        LogsCollectingConfiguration configuration = agentService.addLogsCollectionConfiguration(serviceAddLogsConfiguration);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{agentId}")
                .buildAndExpand(configuration.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new CreateResponse(true, "Configuration Added Successfully", HttpStatus.OK, configuration.getId()));
    }

    @GetMapping("/{agentId}")
    public AgentResponse getAgentById(@PathVariable UUID agentId) {
        log.trace(agentRepository.findById(agentId).get().getName());
        //return agentRepository.findById(agentId).get();
        return agentService.getAgentById(agentId);
    }


}

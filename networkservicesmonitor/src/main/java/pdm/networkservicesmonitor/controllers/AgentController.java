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
import pdm.networkservicesmonitor.payload.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.*;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceResponse;
import pdm.networkservicesmonitor.service.AgentService;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/agent")
public class AgentController {

    // TODO: Each action in agent settings should change updated flag

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

    @PutMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> editAgent(@Valid @RequestBody AgentEditRequest agentEditRequest) {
        agentService.editAgent(agentEditRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{agentId}")
                .buildAndExpand(agentEditRequest.getAgentId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Agent edited successfully", HttpStatus.OK));
    }

    /*@GetMapping("/{agentId}")
    public AgentResponse getAgentById(@PathVariable UUID agentId) {
        return agentService.getAgentById(agentId);
    }*/

    @GetMapping("/details/{agentId}")
    public AgentDetailsResponse getAgentDetailsById(@PathVariable UUID agentId) {
        return agentService.getAgentDetailsById(agentId);
    }

    @DeleteMapping("/{agentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> deleteAgent(@PathVariable UUID agentId) {
        agentService.deleteAgent(agentId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/").build().toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Agent deleted successfully", HttpStatus.OK));
    }

    @GetMapping("/services/{agentId}")
    public PagedResponse<ServiceResponse> getAgentServices(@PathVariable UUID agentId,
                                                           @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                           @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return agentService.getAllAgentServices(agentId, page, size);
    }


}

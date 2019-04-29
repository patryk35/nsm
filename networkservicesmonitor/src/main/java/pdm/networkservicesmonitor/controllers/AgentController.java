package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pdm.networkservicesmonitor.AppConsts;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateResponse;
import pdm.networkservicesmonitor.payload.client.agent.AgentCreateRequest;
import pdm.networkservicesmonitor.payload.client.agent.AgentResponse;
import pdm.networkservicesmonitor.repository.AgentRepository;
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

    /*@GetMapping
    public PagedResponse<AgentResponse> getAgents(@AuthenticationPrincipal UserSecurityDetails currentUser,
                                                  @RequestParam(value = "page", defaultValue = AppConsts.DEFAULT_PAGE_NUMBER) int page,
                                                  @RequestParam(value = "size", defaultValue = AppConsts.DEFAULT_PAGE_SIZE) int size) {
        return agentService.getAllAgents(currentUser, page, size);
    }*/

    @GetMapping
    public PagedResponse<AgentResponse> getAgents(
                                                      @RequestParam(value = "page", defaultValue = AppConsts.DEFAULT_PAGE_NUMBER) int page,
                                                      @RequestParam(value = "size", defaultValue = AppConsts.DEFAULT_PAGE_SIZE) int size) {
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
                .body(new AgentCreateResponse(true, "Agent Created Successfully", HttpStatus.OK,agent.getId(),agent.getEncryptionKey()));
    }

    /*@GetMapping("/{agentId}")
    public AgentResponse getAgentById(@PathVariable UUID agentId) {
        return agentService.getAgentById(agentId);
    }*/
    @GetMapping("/{agentId}")
    public AgentResponse getAgentById(@PathVariable UUID agentId) {
        log.trace(agentRepository.findById(agentId).get().getName());
        //return agentRepository.findById(agentId).get();
        return agentService.getAgentById(agentId);
    }


}

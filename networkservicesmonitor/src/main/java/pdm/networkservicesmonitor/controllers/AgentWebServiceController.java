package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pdm.networkservicesmonitor.payload.agent.AgentRegistrationResponse;
import pdm.networkservicesmonitor.payload.agent.AgentRequest;
import pdm.networkservicesmonitor.payload.agent.configuration.AgentConfigurationResponse;
import pdm.networkservicesmonitor.payload.agent.packet.AgentDataPacket;
import pdm.networkservicesmonitor.payload.agent.packet.AgentDataPacketResponse;
import pdm.networkservicesmonitor.payload.client.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.ApiResponse;
import pdm.networkservicesmonitor.service.AgentWebService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/agent/webservice")
public class AgentWebServiceController {

    @Autowired
    private AgentWebService agentService;

    @PostMapping("/register")
    public ApiResponse registerAgent(@Valid @RequestBody AgentRequest agentRequest) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        agentService.register(agentRequest, request.getRemoteAddr());
        return new ApiBaseResponse(true, "Agent registered", HttpStatus.OK);

    }

    @PostMapping("/checkRegistrationStatus")
    public ApiResponse checkAgentRegistrationStatus(@Valid @RequestBody AgentRequest agentRequest) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        if (agentService.checkRegistrationStatus(agentRequest, request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr())) {
            return new AgentRegistrationResponse(true, "Agent is registered", HttpStatus.OK, true);
        }
        return new AgentRegistrationResponse(true, "Agent is not registered", HttpStatus.OK, false);
    }


    @PostMapping("/getAgentConfiguration")
    public AgentConfigurationResponse getAgentConfiguration(@Valid @RequestBody AgentRequest agentRequest) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        return agentService.getAgentConfiguration(agentRequest, request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr());

    }


    @PostMapping("/agentGateway")
    public AgentDataPacketResponse postData(@Valid @RequestBody AgentDataPacket agentDataPacket) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        return agentService.savePacket(agentDataPacket, request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr());
    }
}

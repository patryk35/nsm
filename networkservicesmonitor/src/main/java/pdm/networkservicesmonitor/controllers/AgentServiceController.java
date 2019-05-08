package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pdm.networkservicesmonitor.payload.agent.*;
import pdm.networkservicesmonitor.payload.client.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.ApiResponse;
import pdm.networkservicesmonitor.repository.AgentRepository;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("${app.apiURL}/agent/service")
public class AgentServiceController {

    @Autowired
    private AgentRepository agentRepository;


    @Autowired
    private AgentService agentService;

    @PostMapping("/register")
    public ApiResponse registerAgent(@Valid @RequestBody AgentRequest agentRequest){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        agentService.register(agentRequest, request.getRemoteAddr());
        return new ApiBaseResponse(true,"Agent registered", HttpStatus.OK);

    }

    @PostMapping("/checkRegistrationStatus")
    public ApiResponse checkAgentRegistrationStatus(@Valid @RequestBody AgentRequest agentRequest){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        if(agentService.checkRegistrationStatus(agentRequest, request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr())){
            return new AgentRegistrationResponse(true,"Agent is registered",HttpStatus.OK,true);
        }
        return new AgentRegistrationResponse(true,"Agent is not registered",HttpStatus.OK,false);
    }


    @PostMapping("/getAgentSettings")
    public AgentSettingsResponse getAgentSettings(@Valid @RequestBody AgentRequest agentRequest){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        return agentService.getAgentSettings(agentRequest, request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr());

    }


    @PostMapping("/agentGateway")
    public AgentDataResponse postData(@Valid @RequestBody AgentDataPackage agentDataPackage){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        agentDataPackage.getLogs().stream().forEach(System.out::println);
        return new AgentDataResponse(true,"", HttpStatus.OK);
        //return agentService.getAgentSettings(agentDataPackage, request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr());

    }
}

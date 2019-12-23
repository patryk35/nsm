package pdm.networkservicesmonitor.agent.controller;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.connection.MonitorWebClient;
import pdm.networkservicesmonitor.agent.controller.exceptions.ProxyDisabledException;
import pdm.networkservicesmonitor.agent.controller.exceptions.ProxyException;
import pdm.networkservicesmonitor.agent.payloads.RegistrationStatusResponseToAgent;
import pdm.networkservicesmonitor.agent.payloads.data.DataPacket;
import pdm.networkservicesmonitor.agent.payloads.proxy.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/agent/webservice")
public class ProxyController {

    @Autowired
    private MonitorWebClient webClient;

    @Autowired
    private AgentConfigurationManager agentConfigurationManager;

    private HashMap<UUID, String> connectedAgentsTokens;
    private HashMap<UUID, String> connectedAgentsOrigins;

    public ProxyController(){
        connectedAgentsOrigins = new HashMap<>();
        connectedAgentsTokens = new HashMap<>();
    }
    @GetMapping(value = "/health")
    public ResponseEntity<?> healthCheck() {
        if (!agentConfigurationManager.isProxy())
            throw new ProxyDisabledException("Proxy is disabled!");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).build();
    }

    @PostMapping("/register")
    public ApiResponse registerAgent(@Valid @RequestBody AgentRequest agentRequest) {
        if (!agentConfigurationManager.isProxy())
            throw new ProxyDisabledException("Proxy is disabled!");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        if(!validateTokenAndRequestIp(agentRequest.getAgentId(), request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr())){
            throw new ProxyException("Invalid token or wrong request ip address(origins)");
        }
        return webClient.registerAgentByProxy(agentRequest);

    }

    @PostMapping("/checkRegistrationStatus")
    public RegistrationStatusResponseToAgent checkAgentRegistrationStatus(@Valid @RequestBody AgentRequest agentRequest) {
        if (!agentConfigurationManager.isProxy())
            throw new ProxyDisabledException("Proxy is disabled!");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        if(!validateTokenAndRequestIp(agentRequest.getAgentId(), request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr())){
            throw new ProxyException("Invalid token or wrong request ip address(origins)");
        }
        return webClient.getRegistrationStatusByProxy(agentRequest);
    }


    @PostMapping("/getAgentConfiguration")
    public AgentConfigurationResponse getAgentConfiguration(@Valid @RequestBody AgentRequest agentRequest) {
        if (!agentConfigurationManager.isProxy())
            throw new ProxyDisabledException("Proxy is disabled!");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        if(!validateTokenAndRequestIp(agentRequest.getAgentId(), request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr())){
            throw new ProxyException("Invalid token or wrong request ip address(origins)");
        }
        return webClient.downloadAgentConfigurationByProxy(agentRequest);

    }

    @PostMapping("/checkAgentConfigurationUpdates")
    public AgentConfigurationUpdatesAvailabilityResponse checkAgentConfigurationUpdates(@Valid @RequestBody AgentRequest agentRequest) {
        if (!agentConfigurationManager.isProxy())
            throw new ProxyDisabledException("Proxy is disabled!");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        if(!validateTokenAndRequestIp(agentRequest.getAgentId(), request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr())){
            throw new ProxyException("Invalid token or wrong request ip address(origins)");
        }
        return webClient.checkConfigurationUpdatesByProxy(agentRequest);
    }


    @PostMapping("/agentGateway")
    public AgentDataPacketResponse postData(@Valid @RequestBody DataPacket agentDataPacket) {
        if (!agentConfigurationManager.isProxy())
            throw new ProxyDisabledException("Proxy is disabled!");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        if(!validateTokenAndRequestIp(agentDataPacket.getAgentId(), request.getHeader(HttpHeaders.AUTHORIZATION), request.getRemoteAddr())){
            throw new ProxyException("Invalid token or wrong request ip address(origins)");
        }

        return webClient.sendPacketByProxy(agentDataPacket);
    }

    public boolean validateTokenAndRequestIp(UUID agentId, String token, String ip) {
        if(!connectedAgentsTokens.containsKey(agentId) || !connectedAgentsOrigins.containsKey(agentId)){
            webClient.validateTokenAndRequestIp(token, ip);
            connectedAgentsOrigins.put(agentId, ip);
            connectedAgentsTokens.put(agentId, token);
            return true;
        }
        if(!(connectedAgentsTokens.get(agentId).equals(token))){
            webClient.validateTokenAndRequestIp(token, ip);
            connectedAgentsOrigins.put(agentId, ip);
            connectedAgentsTokens.put(agentId, token);
            return true;
        } else if (!(connectedAgentsOrigins.get(agentId).equals(ip))){
            webClient.validateTokenAndRequestIp(token, ip);
            connectedAgentsOrigins.put(agentId, ip);
            connectedAgentsTokens.put(agentId, token);
            return true;
        }
        return true;
    }
}
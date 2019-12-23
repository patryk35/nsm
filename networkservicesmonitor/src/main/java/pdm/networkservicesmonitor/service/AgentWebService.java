package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.NetworkServicesMonitorApplication;
import pdm.networkservicesmonitor.exceptions.MethodNotAllowed;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.model.agent.AgentConfiguration;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.payload.agent.AgentRequest;
import pdm.networkservicesmonitor.payload.agent.configuration.AgentConfigurationResponse;
import pdm.networkservicesmonitor.payload.agent.configuration.ServiceConfiguration;
import pdm.networkservicesmonitor.payload.agent.packet.AgentDataPacket;
import pdm.networkservicesmonitor.payload.agent.packet.AgentDataPacketResponse;
import pdm.networkservicesmonitor.repository.AgentConfigurationRepository;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.security.jwt.JwtTokenProvider;
import pdm.networkservicesmonitor.service.util.DataPacketWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.convertStringToList;

@Service
@Slf4j
public class AgentWebService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentConfigurationRepository agentConfigurationRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AgentConfigurationResponse getAgentConfiguration(AgentRequest agentRequest, String authToken,
                                                            String requestIp) {
        MonitorAgent monitorAgent = getAgentWithVerification(agentRequest.getAgentId(), authToken, requestIp);
        return createAgentConfigurationResponse(monitorAgent);
    }

    public boolean checkRegistrationStatus(AgentRequest agentRequest, String authToken, String requestIp) {
        MonitorAgent monitorAgent = getAgentWithVerification(agentRequest.getAgentId(), authToken, requestIp);
        return monitorAgent.isRegistered();
    }


    public AgentDataPacketResponse savePacket(AgentDataPacket agentDataPacket, String authToken, String requestIp) {
        MonitorAgent monitorAgent = getAgentWithVerification(agentDataPacket.getAgentId(), authToken, requestIp);
        NetworkServicesMonitorApplication.addPacketToQueue(new DataPacketWrapper(agentDataPacket, monitorAgent));
        return new AgentDataPacketResponse(monitorAgent.getId(), agentDataPacket.getPacketId());
    }

    public void register(AgentRequest agentRequest, String requestIp) {
        MonitorAgent agent = agentRepository.findById(agentRequest.getAgentId()).orElseThrow(() ->
                new NotFoundException("Agent not found. Agent id or encryptionKey not valid"));

        if (agent.isRegistered()) {
            throw new MethodNotAllowed("Agent is already registered");
        }

        if (agent.getAllowedOrigins().isEmpty()) {
            agent.setAllowedOrigins(convertStringToList(requestIp, ","));
        } else if (!filterRequestIp(requestIp, agent.getAllowedOrigins())) {
            throw new MethodNotAllowed("Current ip address not in allowed origins. " +
                    "Set appropriate allowed origins or left it blank to auto fill");
        }

        agent.setRegistered(true);
        agentRepository.save(agent);
    }

    public boolean checkAgentConfigurationUpdates(AgentRequest agentRequest, String authToken, String requestIp) {
        MonitorAgent monitorAgent = getAgentWithVerification(agentRequest.getAgentId(), authToken, requestIp);
        if (monitorAgent.getAgentConfiguration().isUpdated()) {
            monitorAgent.getAgentConfiguration().setUpdated(false);
            agentConfigurationRepository.save(monitorAgent.getAgentConfiguration());
            return true;
        }
        return false;
    }

    public boolean verifyAgentTokenAndOrigins(String token, String requestIp) {
        MonitorAgent agent = jwtTokenProvider.validateAgentToken(token);
        return agent != null && (!agent.getAllowedOrigins().isEmpty() || filterRequestIp(requestIp, agent.getAllowedOrigins()));
    }

    private AgentConfigurationResponse createAgentConfigurationResponse(MonitorAgent monitorAgent) {
        AgentConfiguration agentConfiguration = monitorAgent.getAgentConfiguration();
        List<ServiceConfiguration> servicesConfiguration = new ArrayList<>();
        monitorAgent.getServices().forEach(service -> {
            service.getMonitoredParametersConfigurations().parallelStream()
                    .forEach(m -> {
                        m.setParameterId(m.getParameterType().getId());
                        m.setParameterParentId(m.getParameterType().getParentId());
                    });
            servicesConfiguration.add(new ServiceConfiguration(
                    service.getId(),
                    service.getLogsCollectingConfigurations().parallelStream().filter(c -> !c.isDeleted()).collect(Collectors.toList()),
                    service.getMonitoredParametersConfigurations().parallelStream().filter(c -> !c.isDeleted()).collect(Collectors.toList())
            ));
        });
        return new AgentConfigurationResponse(monitorAgent.getId(), agentConfiguration.getSendingInterval(),
                servicesConfiguration, monitorAgent.isProxyAgent());
    }


    private boolean filterRequestIp(String requestIp, List<String> allowedOrigins) {
        if (allowedOrigins.contains(requestIp)) {
            return true;
        }
        return allowedOrigins.contains("*");
    }

    private MonitorAgent getAgentWithVerification(UUID agentId, String authToken, String requestIp) {
        MonitorAgent tokenAgent = jwtTokenProvider.validateAgentToken(authToken);
        if (tokenAgent == null) {
            throw new NotFoundException(String.format(
                    "Agent %s not found. Agent id or encryptionKey not valid",
                    agentId.toString()
            ));
        }

        if (!tokenAgent.getAllowedOrigins().isEmpty() && !filterRequestIp(requestIp, tokenAgent.getAllowedOrigins())) {
            throw new MethodNotAllowed("Current ip address not in allowed origins. " +
                    "Set appropriate allowed origins or left it blank to auto fill");
        }
        return agentRepository.findById(agentId).orElseThrow(() -> new NotFoundException(String.format(
                "Agent %s not found. Agent id or encryptionKey not valid",
                agentId.toString())
        ));
    }


}

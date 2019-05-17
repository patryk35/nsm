package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.exceptions.MethodNotAllowed;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.AgentConfiguration;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;
import pdm.networkservicesmonitor.payload.agent.AgentRequest;
import pdm.networkservicesmonitor.payload.agent.configuration.AgentConfigurationResponse;
import pdm.networkservicesmonitor.payload.agent.configuration.ServiceConfiguration;
import pdm.networkservicesmonitor.payload.agent.packet.AgentDataPacket;
import pdm.networkservicesmonitor.payload.agent.packet.AgentDataPacketResponse;
import pdm.networkservicesmonitor.repository.*;
import pdm.networkservicesmonitor.security.jwt.JwtTokenProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static pdm.networkservicesmonitor.service.AgentServicesUtil.convertOriginsToList;

@Service
@Slf4j
public class AgentWebService {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private MonitoredParameterTypeRepository monitoredParameterTypeRepository;
    @Autowired
    private MonitoredParametersValuesRepository monitoredParametersValuesRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CollectedLogsRepository collectedLogsRepository;

    public AgentConfigurationResponse getAgentConfiguration(AgentRequest agentRequest, String authToken, String requestIp) {
        MonitorAgent monitorAgent = getAgentWithVerification(agentRequest.getAgentId(), authToken, requestIp);
        return createAgentConfigurationResponse(monitorAgent);
    }

    public boolean checkRegistrationStatus(AgentRequest agentRequest, String authToken, String requestIp) {
        MonitorAgent monitorAgent = getAgentWithVerification(agentRequest.getAgentId(), authToken, requestIp);
        return monitorAgent.isRegistered();
    }


    public AgentDataPacketResponse savePacket(AgentDataPacket agentDataPacket, String authToken, String requestIp) {
        MonitorAgent monitorAgent = getAgentWithVerification(agentDataPacket.getAgentId(), authToken, requestIp);

        agentDataPacket.getLogs().forEach(serviceLogs -> {
            // TODO(high): It should reject only one service logs, not all packet - find some resolution for it

            pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(serviceLogs.getServiceId()).orElseThrow(() -> new NotFoundException(String.format("Service %s not found. Service serviceId not valid", serviceLogs.getServiceId().toString())));
            if (service.getAgent().getId() != monitorAgent.getId()) {
                throw new ResourceNotFoundException("service", "id", service.getId());
            }
            serviceLogs.getLogs().forEach(logEntry -> {
                CollectedLog collectedLog = new CollectedLog(service, serviceLogs.getPath(), logEntry.getTimestamp(), logEntry.getLog());
                collectedLogsRepository.save(collectedLog);
            });
        });
        agentDataPacket.getMonitoring().forEach(serviceMonitoringParameters -> {
            pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(serviceMonitoringParameters.getServiceId()).orElseThrow(() -> new NotFoundException(String.format("Service %s not found. Service serviceId not valid", serviceMonitoringParameters.getServiceId().toString())));
            if (service.getAgent().getId() != monitorAgent.getId()) {
                throw new ResourceNotFoundException("service", "id", service.getId());
            }
            MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository.findById(serviceMonitoringParameters.getParameterId()).orElseThrow(() -> new NotFoundException(String.format("Monitored Parameter %s not found. ParameterId not valid", serviceMonitoringParameters.getParameterId().toString())));
            serviceMonitoringParameters.getMonitoredParameters().forEach(monitoredParameterEntry -> {
                MonitoredParameterValue monitoredParameterValue = new MonitoredParameterValue(monitoredParameterType, service, monitoredParameterEntry.getTimestamp(), monitoredParameterEntry.getValue());
                monitoredParametersValuesRepository.save(monitoredParameterValue);
            });
        });

        if (monitorAgent.getAgentConfiguration().getConfigurationVersion() != agentDataPacket.getConfigurationVersion()) {
            return new AgentDataPacketResponse(monitorAgent.getId(), agentDataPacket.getPacketId(), createAgentConfigurationResponse(monitorAgent));
        } else {
            return new AgentDataPacketResponse(monitorAgent.getId(), agentDataPacket.getPacketId(), null);
        }
    }

    public void register(AgentRequest agentRequest, String requestIp) {
        MonitorAgent agent = agentRepository.findById(agentRequest.getAgentId()).orElseThrow(() ->
                new NotFoundException("Agent not found. Agent id or encryptionKey not valid"));

        if (agent.isRegistered()) {
            throw new MethodNotAllowed("Agent is already registered");
        }

        if (agent.getAllowedOrigins().isEmpty()) {
            agent.setAllowedOrigins(convertOriginsToList(requestIp));
        } else if (!filterRequestIp(requestIp, agent.getAllowedOrigins())) {
            throw new MethodNotAllowed("Current ip address not in allowed origins. Set appropriate allowed origins or left it blank to auto fill");
        }

        agent.setRegistered(true);
        agentRepository.save(agent);
    }

    private AgentConfigurationResponse createAgentConfigurationResponse(MonitorAgent monitorAgent) {
        AgentConfiguration agentConfiguration = monitorAgent.getAgentConfiguration();
        List<ServiceConfiguration> servicesConfiguration = new ArrayList<>();
        monitorAgent.getServices().forEach(service -> {
            service.getMonitoredParametersConfigurations().parallelStream().forEach(m -> m.setParameterId(m.getParameterType().getId()));
            servicesConfiguration.add(new ServiceConfiguration(
                    service.getId(),
                    service.getLogsCollectingConfigurations(),
                    service.getMonitoredParametersConfigurations()
            ));
        });
        return new AgentConfigurationResponse(monitorAgent.getId(), agentConfiguration.getSendingInterval(),
                servicesConfiguration);
    }

    private boolean filterRequestIp(String requestIp, List<String> allowedOrigins) {
        if (allowedOrigins.contains(requestIp)) {
            return true;
        }
        return allowedOrigins.contains("*");
        // TODO(high): check if requestIp is in allowedOrigins, some filter to check masks, partial addresses
    }

    private MonitorAgent getAgentWithVerification(UUID agentId, String authToken, String requestIp) {
        MonitorAgent agent = agentRepository.findById(agentId).orElseThrow(() -> new NotFoundException(String.format("Agent %s not found. Agent id or encryptionKey not valid", agentId.toString())));
        if (!jwtTokenProvider.validateAgentToken(authToken, agent.getEncryptionKey())) {
            throw new NotFoundException(String.format("Agent %s not found. Agent id or encryptionKey not valid", agentId.toString()));
        }

        if (!agent.getAllowedOrigins().isEmpty() && !filterRequestIp(requestIp, agent.getAllowedOrigins())) {
            throw new MethodNotAllowed("Current ip address not in allowed origins. Set appropriate allowed origins or left it blank to auto fill");
        }
        return agent;
    }
}

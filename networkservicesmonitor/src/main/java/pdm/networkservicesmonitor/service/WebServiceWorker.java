package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.NetworkServicesMonitorApplication;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.model.data.DataPacketWrapper;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;
import pdm.networkservicesmonitor.payload.agent.packet.AgentDataPacket;
import pdm.networkservicesmonitor.repository.*;
import pdm.networkservicesmonitor.security.jwt.JwtTokenProvider;

@Slf4j
@Component("webServiceWorker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WebServiceWorker implements Runnable {

    // TODO: Change pub -> priv after fixing WebServiceWorker - it not resolving this annotation - probably, because it is not a bean

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private MonitoredParameterTypeRepository monitoredParameterTypeRepository;
    @Autowired
    private MonitoredParametersValuesRepository monitoredParametersValuesRepository;
    @Autowired
    private CollectedLogsRepository collectedLogsRepository;

    @Override
    public void run() {
        log.trace("Starting worker");
        while(true){
            DataPacketWrapper dataPacketWrapper;
            while((dataPacketWrapper = NetworkServicesMonitorApplication.getPacketFromQueue()) != null){
                AgentDataPacket agentDataPacket = dataPacketWrapper.getAgentDataPacket();
                MonitorAgent monitorAgent = dataPacketWrapper.getMonitorAgent();
                agentDataPacket.getLogs().forEach(serviceLogs -> {
                    // TODO(high): It should reject only one service logs, not all packet - find some resolution for it
                    // TODO(high): inventory of received packages
                    pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(serviceLogs.getServiceId()).orElseThrow(() -> new NotFoundException(String.format("Service %s not found. Service serviceId not valid", serviceLogs.getServiceId().toString())));
                    if (!monitorAgent.getId().equals(service.getAgent().getId())) {
                        throw new ResourceNotFoundException(String.format("agent %s service", service.getAgent().getId()), "id", service.getId());
                    }
                    serviceLogs.getLogs().forEach(logEntry -> {
                        CollectedLog collectedLog = new CollectedLog(service, serviceLogs.getPath(), logEntry.getTimestamp(), logEntry.getLog());
                        collectedLogsRepository.save(collectedLog);
                    });
                });
                agentDataPacket.getMonitoring().forEach(serviceMonitoringParameters -> {
                    pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(serviceMonitoringParameters.getServiceId()).orElseThrow(() -> new NotFoundException(String.format("Service %s not found. Service serviceId not valid", serviceMonitoringParameters.getServiceId().toString())));
                    if (!monitorAgent.getId().equals(service.getAgent().getId())) {
                        log.trace(monitorAgent.getId().toString());
                        log.trace((service.getAgent()).getId().toString());
                        throw new ResourceNotFoundException(String.format("agent %s service", service.getAgent().getId()), "id", service.getId());
                    }
                    MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository.findById(serviceMonitoringParameters.getParameterId()).orElseThrow(() -> new NotFoundException(String.format("Monitored Parameter %s not found. ParameterId not valid", serviceMonitoringParameters.getParameterId().toString())));
                    serviceMonitoringParameters.getMonitoredParameters().forEach(monitoredParameterEntry -> {
                        MonitoredParameterValue monitoredParameterValue = new MonitoredParameterValue(monitoredParameterType, service, monitoredParameterEntry.getTimestamp(), monitoredParameterEntry.getValue());
                        monitoredParametersValuesRepository.save(monitoredParameterValue);
                    });
                });
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

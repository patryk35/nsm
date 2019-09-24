package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddLogsConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceAddMonitoredParameterConfigurationRequest;
import pdm.networkservicesmonitor.payload.client.agent.service.ServiceCreateRequest;
import pdm.networkservicesmonitor.repository.*;

@Service
@Slf4j
public class AgentServicesService {

    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private LogsCollectingConfigurationRepository logsCollectingConfigurationRepository;
    @Autowired
    private MonitoredParameterTypeRepository monitoredParameterTypeRepository;
    @Autowired
    private MonitoredParameterConfigurationRepository monitoredParameterConfigurationRepository;

    public pdm.networkservicesmonitor.model.agent.service.Service createService(ServiceCreateRequest serviceCreateRequest) {
        MonitorAgent agent = agentRepository.findById(serviceCreateRequest.getAgentId()).orElseThrow(() ->
                new NotFoundException("Agent not found. Agent id is not valid"));
        pdm.networkservicesmonitor.model.agent.service.Service service =
                new pdm.networkservicesmonitor.model.agent.service.Service(serviceCreateRequest.getName(), serviceCreateRequest.getDescription(), agent);
        agent.addService(service);
        if (agentRepository.save(agent) != null) {
            return serviceRepository.save(service);
        }
        return null;
    }

    public LogsCollectingConfiguration addLogsCollectionConfiguration(ServiceAddLogsConfigurationRequest configurationRequest) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        LogsCollectingConfiguration logsCollectingConfiguration = new LogsCollectingConfiguration(configurationRequest.getPath(), configurationRequest.getMonitoredFilesMask(), configurationRequest.getLogLineRegex(), service);
        return logsCollectingConfigurationRepository.save(logsCollectingConfiguration);
    }

    public MonitoredParameterConfiguration addMonitoredParameterConfiguration(ServiceAddMonitoredParameterConfigurationRequest configurationRequest) {
        pdm.networkservicesmonitor.model.agent.service.Service service = serviceRepository.findById(configurationRequest.getServiceId()).orElseThrow(() ->
                new NotFoundException("Service not found. Service id is not valid"));
        MonitoredParameterType monitoredParameterType = monitoredParameterTypeRepository.findById(configurationRequest.getParameterTypeId()).orElseThrow(() ->
                new NotFoundException(("Parameter type not found. Parameter type is not valid")));
        MonitoredParameterConfiguration monitoredParameterConfiguration = new MonitoredParameterConfiguration(monitoredParameterType, service, configurationRequest.getDescription(), configurationRequest.getMonitoringInterval());
        return monitoredParameterConfigurationRepository.save(monitoredParameterConfiguration);
    }
}

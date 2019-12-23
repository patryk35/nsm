package pdm.networkservicesmonitor.agent.worker.specializedWorkers.custom;

import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;

import java.util.UUID;

public class PortOpenConnections extends ProcessWorker {
    public PortOpenConnections(ConnectionWorker connectionWorker, UUID serviceId, MonitoredParameterConfiguration monitoredParameterConfiguration) {
        super(connectionWorker,
                serviceId,
                monitoredParameterConfiguration.getId(),
                monitoredParameterConfiguration.getParameterId(),
                monitoredParameterConfiguration.getMonitoringInterval()
        );
        processBuilder.command("bash", "-c", String.format(
                "netstat -anp | grep \":%s\" | grep ESTABLISHED | wc -l",
                monitoredParameterConfiguration.getTargetObject())
        );
    }
}
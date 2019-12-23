package pdm.networkservicesmonitor.agent.worker.specializedWorkers.custom;

import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;

import java.util.UUID;

public class ProcessesMemoryUsage extends ProcessWorker {
    public ProcessesMemoryUsage(ConnectionWorker connectionWorker, UUID serviceId, MonitoredParameterConfiguration monitoredParameterConfiguration) {
        super(connectionWorker,
                serviceId,
                monitoredParameterConfiguration.getId(),
                monitoredParameterConfiguration.getParameterId(),
                monitoredParameterConfiguration.getMonitoringInterval()
        );
        processBuilder.command("bash", "-c", String.format(
                "ps -ao %%mem,cmd | grep -e \"%s\" | grep -v grep | awk '{ print $1; }' | jq -s 'add' | awk '{print $1/100}'",
                monitoredParameterConfiguration.getTargetObject())
        );
    }
}

package pdm.networkservicesmonitor.agent.worker.specializedWorkers.custom;

import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;

import java.util.UUID;

public class ProcessesCountForCommand extends ProcessWorker {
    public ProcessesCountForCommand(ConnectionWorker connectionWorker, UUID serviceId, MonitoredParameterConfiguration monitoredParameterConfiguration) {
        super(connectionWorker,
                serviceId,
                monitoredParameterConfiguration.getId(),
                monitoredParameterConfiguration.getParameterId(),
                monitoredParameterConfiguration.getMonitoringInterval()
        );
        //initWorker("scripts/loadProcessData.sh", "pc", monitoredParameterConfiguration.getTargetObject());
        processBuilder.command("bash", "-c", String.format(
                "ps -ao cmd | grep -e \"%s\" | grep -v grep | awk '{ print $1; }' | wc -l",
                monitoredParameterConfiguration.getTargetObject())
        );
    }
}

package pdm.networkservicesmonitor.agent.worker.specializedWorkers.custom;

import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;
import pdm.networkservicesmonitor.agent.worker.WorkerException;
import pdm.networkservicesmonitor.agent.worker.specializedWorkers.MonitoringWorker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class DirectorySizeWorker extends MonitoringWorker {
    private String monitoredPath;

    public DirectorySizeWorker(ConnectionWorker connectionWorker, UUID serviceId, MonitoredParameterConfiguration monitoredParameterConfiguration) {
        super(connectionWorker,
                serviceId,
                monitoredParameterConfiguration.getId(),
                monitoredParameterConfiguration.getParameterId(),
                monitoredParameterConfiguration.getMonitoringInterval());
        this.monitoredPath = monitoredParameterConfiguration.getTargetObject();
    }

    @Override
    public String getMonitoredValue() {
        Path folder = Paths.get(monitoredPath);
        long size = 0;
        try {
            size = Files.walk(folder)
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        } catch (Exception e) {
            isRunning = false;
            throw new WorkerException(String.format("Problems during checking %s: %s", monitoredPath, e.getMessage()));
        }

        return String.valueOf(size);
    }
}

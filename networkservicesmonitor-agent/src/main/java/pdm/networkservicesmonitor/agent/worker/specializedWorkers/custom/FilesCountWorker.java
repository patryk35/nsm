package pdm.networkservicesmonitor.agent.worker.specializedWorkers.custom;

import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;
import pdm.networkservicesmonitor.agent.worker.WorkerException;
import pdm.networkservicesmonitor.agent.worker.specializedWorkers.MonitoringWorker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public class FilesCountWorker extends MonitoringWorker {
    private Path monitoredPath;
    public FilesCountWorker(ConnectionWorker connectionWorker, UUID serviceId, MonitoredParameterConfiguration monitoredParameterConfiguration) {
        super(connectionWorker,
                serviceId,
                monitoredParameterConfiguration.getId(),
                monitoredParameterConfiguration.getParameterId(),
                monitoredParameterConfiguration.getMonitoringInterval());
        this.monitoredPath = Path.of(monitoredParameterConfiguration.getTargetObject());
    }

    @Override
    public String getMonitoredValue() {
        if (!Files.exists(monitoredPath)) {
            throw new WorkerException(String.format("Path %s not exists", monitoredPath));
        } else if (!Files.isDirectory(monitoredPath)) {
            throw new WorkerException(String.format("Path %s is not a directory", monitoredPath));
        }
        return String.valueOf(Objects.requireNonNull(new File(monitoredPath.toString()).list()).length);
    }
}
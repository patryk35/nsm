package pdm.networkservicesmonitor.agent.worker.specializedWorkers.custom;

import org.springframework.web.client.ResourceAccessException;
import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;
import pdm.networkservicesmonitor.agent.worker.WorkerException;
import pdm.networkservicesmonitor.agent.worker.specializedWorkers.MonitoringWorker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
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
import java.nio.file.Paths;
import java.util.UUID;


public class ProcessWorker extends MonitoringWorker {
    protected ProcessBuilder processBuilder;
    public ProcessWorker(ConnectionWorker connectionWorker, UUID serviceId, UUID configurationId, UUID parameterId, long monitoringInterval) {
        super(connectionWorker,
                serviceId,
                configurationId,
                parameterId,
                monitoringInterval
        );
        processBuilder = new ProcessBuilder();
    }

    protected void initWorker(String scriptPath, String option, String targetObject){
        if(!scriptPath.contains("portOpenConnections.sh")){
            File file = getFile(scriptPath);
            String absolutePath = file.getAbsolutePath();
            processBuilder.command(absolutePath, option, targetObject);
        } else {
            File file = getFile(scriptPath);
            String absolutePath = file.getAbsolutePath();
            processBuilder.command(absolutePath, targetObject);
        }
    }

    private File getFile(String path){
        URL res = getClass().getClassLoader().getResource(path);
        try {
            return Paths.get(res.toURI()).toFile();
        } catch (URISyntaxException e) {
            throw new ResourceAccessException(String.format("Agent - script %s not found in application resources", path));
        }
    }

    @Override
    public String getMonitoredValue() {
        String output = null;

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output=line;
            }
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                throw new WorkerException(String.format("Script exited with status %d", exitVal));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new WorkerException(String.format("Reading script output problems: %s", e.getMessage()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new WorkerException(String.format("Data collecting script interrupted: %s", e.getMessage()));
        }

        if(output == null){
            return "0";
        }
        return String.valueOf(Double.parseDouble(output));
    }
}

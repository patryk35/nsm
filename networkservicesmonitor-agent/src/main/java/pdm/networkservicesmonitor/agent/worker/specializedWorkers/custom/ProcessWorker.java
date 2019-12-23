package pdm.networkservicesmonitor.agent.worker.specializedWorkers.custom;

import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;
import pdm.networkservicesmonitor.agent.worker.WorkerException;
import pdm.networkservicesmonitor.agent.worker.specializedWorkers.MonitoringWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    @Override
    public String getMonitoredValue() {
        String output = null;

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output = line;
            }
            int exitVal = process.waitFor();
            if (exitVal != 0) {
                isRunning = false;
                throw new WorkerException(String.format("Script exited with status %d", exitVal));
            }
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
            throw new WorkerException(String.format("Reading script output problems: %s", e.getMessage()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            isRunning = false;
            throw new WorkerException(String.format("Data collecting script interrupted: %s", e.getMessage()));
        }

        if (output == null) {
            return "0";
        }
        return String.valueOf(Double.parseDouble(output));
    }
}

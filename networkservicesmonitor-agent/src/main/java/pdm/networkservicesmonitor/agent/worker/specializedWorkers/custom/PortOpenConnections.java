package pdm.networkservicesmonitor.agent.worker.specializedWorkers.custom;

import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;
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

public class PortOpenConnections extends MonitoringWorker {
    public PortOpenConnections(ConnectionWorker connectionWorker, UUID serviceId, MonitoredParameterConfiguration monitoredParameterConfiguration) {
        super(connectionWorker,
                serviceId,
                monitoredParameterConfiguration.getId(),
                monitoredParameterConfiguration.getParameterId(),
                monitoredParameterConfiguration.getMonitoringInterval());
    }

    @Override
    public String getMonitoredValue() {
        URL res = getClass().getClassLoader().getResource("scripts/portOpenConnections.sh");
        File file = null;
        try {
            file = Paths.get(res.toURI()).toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String absolutePath = file.getAbsolutePath();
        ProcessBuilder processBuilder = new ProcessBuilder();
        String output = null;


        // -- Linux --

        // Run a shell command
        //processBuilder.command("bash", "-c", "ls /home/mkyong/");

        // Run a shell script
        processBuilder.command(absolutePath);

        // -- Windows --

        // Run a command
        //processBuilder.command("cmd.exe", "/c", "dir C:\\Users\\mkyong");

        // Run a bat file
        //processBuilder.command("C:\\Users\\mkyong\\hello.bat");

        try {

            Process process = processBuilder.start();


            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output=line;
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {

            } else {
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return String.valueOf(Integer.parseInt(output.toString()));
    }
}
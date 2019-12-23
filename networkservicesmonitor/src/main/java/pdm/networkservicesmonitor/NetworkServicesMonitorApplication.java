package pdm.networkservicesmonitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import pdm.networkservicesmonitor.service.util.DataPacketWrapper;
import pdm.networkservicesmonitor.workers.AlertsWorkersManager;
import pdm.networkservicesmonitor.workers.WebServiceWorkersManager;
import pdm.networkservicesmonitor.workers.WorkersUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@EntityScan(basePackageClasses = {
        NetworkServicesMonitorApplication.class,
        Jsr310JpaConverters.class
})

@Slf4j
public class NetworkServicesMonitorApplication {

    private static ObjectMapper objectMapper;
    private static Queue<DataPacketWrapper> dataPacketWrapperQueue = new LinkedBlockingQueue<>();
    private static File temporaryFolder = new File("tmp");
    @Autowired
    private ApplicationContext appContext;

    public static void main(String[] args) {
        SpringApplication.run(NetworkServicesMonitorApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                log.info("Executing onExit actions ...");
                savePacketsToFiles(temporaryFolder);
            }
        });
    }

    public static synchronized void addPacketToQueue(DataPacketWrapper dataPacketWrapper) {
        dataPacketWrapperQueue.add(dataPacketWrapper);
    }

    public static synchronized DataPacketWrapper getPacketFromQueue() {
        return dataPacketWrapperQueue.poll();
    }

    public static synchronized int getQueueSize() {
        return dataPacketWrapperQueue.size();
    }

    @PostConstruct
    protected void init() {
        loadPacketsFromFiles(temporaryFolder);
        ((WebServiceWorkersManager) appContext.getBean("webServiceWorkersManager")).start();
        ((AlertsWorkersManager) appContext.getBean("alertsWorkersManager")).start();
    }

    private static void savePacketsToFiles(File temporaryFolder) {
        if (!temporaryFolder.exists()) {
            try {
                log.warn(String.format("Temporary directory %s was removed. Creating new one.", temporaryFolder.getAbsolutePath()));
                Files.createDirectories(temporaryFolder.toPath());
            } catch (IOException ex) {
                log.error("Critical error, application will exit. Cannot create temporary directory. Make sure that application has access to path " + temporaryFolder.getAbsolutePath());
                log.error(ex.getMessage());
                // Without this directory agent cannot work properly
                System.exit(-10404);
            }
        }

        String fileName = String.format("%s/packets_%s.dat", temporaryFolder.getAbsolutePath(), UUID.randomUUID().toString());

        try {
            objectMapper.writeValue(new File(fileName), dataPacketWrapperQueue);
            log.info(String.format("All %d packets queue entries saved to files.", dataPacketWrapperQueue.size()));
            dataPacketWrapperQueue.clear();
        } catch (IOException ex) {
            log.error("Cannot save packet data to file w");
            log.error(ex.getMessage());
        }
    }

    private static void loadPacketsFromFiles(File temporaryFolder) {
        File[] listOfFiles = temporaryFolder.listFiles((dir, name) -> name.matches("packet.*"));
        if (listOfFiles != null) {
            Arrays.stream(listOfFiles).forEach(file -> {
                log.debug("Reading content form file: " + file.getAbsolutePath());
                String json = WorkersUtils.getFileContent(file.getAbsolutePath());
                Queue<DataPacketWrapper> queue = WorkersUtils.fromJSON(new TypeReference<>() {
                }, json);
                while (!queue.isEmpty()) {
                    dataPacketWrapperQueue.add(queue.poll());
                }
                file.delete();
            });
        }
    }
}

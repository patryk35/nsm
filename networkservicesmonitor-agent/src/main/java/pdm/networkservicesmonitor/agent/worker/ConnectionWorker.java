package pdm.networkservicesmonitor.agent.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.AgentApplication;
import pdm.networkservicesmonitor.agent.configuration.AppConstants;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.connection.MonitorWebClient;
import pdm.networkservicesmonitor.agent.payloads.UpdatesAvailabilityMonitorResponse;
import pdm.networkservicesmonitor.agent.payloads.data.DataPacket;
import pdm.networkservicesmonitor.agent.payloads.data.LogEntry;
import pdm.networkservicesmonitor.agent.payloads.data.MonitoredParameterEntry;
import pdm.networkservicesmonitor.agent.worker.utils.WorkersUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConnectionWorker implements Runnable {

    @Value("${agent.id}")
    private UUID agentId;

    @Value("${app.temporary.directory}")
    private String temporaryPath;


    @Autowired
    private MonitorWebClient monitorWebClient;

    @Autowired
    private AgentConfigurationManager agentConfigurationManager;

    @Getter
    private boolean isLocked;

    private Queue<DataPacket> packetQueue;

    @Getter
    private List<LogEntry> internalProblemsLogs;

    private Date date;

    private ConcurrentHashMap<UUID, ServiceDataEntries> dataPacketEntries;

    private ObjectMapper objectMapper;
    public ConnectionWorker() {
        date = new Date();
        isLocked = false;
        packetQueue = new ArrayBlockingQueue<>(AppConstants.MAX_PACKETS_IN_SENDING_QUEUE + 1);
        dataPacketEntries = new ConcurrentHashMap<>();
        internalProblemsLogs = new ArrayList<>();
        objectMapper = new ObjectMapper();
        AgentApplication.setConnectionWorker(this);
    }

    @Override
    public void run() {
        File temporaryFolder = new File(temporaryPath);

        if (!temporaryFolder.exists()) {
            try {
                Files.createDirectories(temporaryFolder.toPath());
            } catch (IOException ex) {
                log.error("Critical error, application will exit. Cannot create temporary directory. Make sure that application has access to path " + temporaryFolder.getAbsolutePath());
                log.error(ex.getMessage());
                System.exit(-10404);
            }
        }

        while (true) {
            try{
                checkConfigurationUpdates();
                try {
                    Thread.sleep(agentConfigurationManager.getSendingInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DataPacket dataPacket = createPacket();

                try {
                    monitorWebClient.testMonitorConnection();
                    sendPackagesFormFiles(temporaryFolder);
                    sendPackagesFromQueue();
                    monitorWebClient.sendPacket(dataPacket);
                    log.trace("All packets sent");
                } catch (Exception e) {
                    log.error(String.format("Packet cannot be send due to connection problems: %s", e.getMessage()));
                    if (packetQueue.size() >= AppConstants.MAX_PACKETS_IN_SENDING_QUEUE) {
                        savePacketsToFiles(temporaryFolder);
                    }
                    packetQueue.add(dataPacket);
                }
            } catch (Exception e) {
                log.error(String.format("Connections problems: %s", e.getMessage()));
            }
        }
    }

    private void checkConfigurationUpdates() {
        try {
            UpdatesAvailabilityMonitorResponse response = monitorWebClient.checkConfigurationUpdates();

            if (response.getUpdated()) {
                agentConfigurationManager.updateConfiguration();
            }
        } catch (Exception e) {
            log.error(String.format("Updates availability cannot be checked due to connection problems: %s", e.getMessage()));
        }
    }

    private void savePacketsToFiles(File temporaryFolder) {
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

        String fileName = String.format("%s/packets_%s.dat", temporaryPath, UUID.randomUUID().toString());

        try {
            objectMapper.writeValue(new File(fileName), packetQueue);
            log.info(String.format("All %d packets queue entries saved to files. Packets will be loaded and send when connection will be opened", packetQueue.size()));
            packetQueue.clear();
        } catch (IOException ex) {
            log.error("Cannot save packet data too file when cleaning packet queue when queue full");
            log.error(ex.getMessage());
        }
    }

    private void sendPackagesFromQueue() {
        while (!packetQueue.isEmpty()) {
            monitorWebClient.sendPacket(packetQueue.peek());
            packetQueue.poll();
            log.trace("Packet was sent");
        }
    }

    private void sendPackagesFormFiles(File temporaryFolder) {
        File[] listOfFiles = temporaryFolder.listFiles((dir, name) -> name.matches("packet.*"));
        if (listOfFiles != null) {
            Arrays.stream(listOfFiles).forEach(file -> {
                log.debug("Reading content form file: " + file.getAbsolutePath());
                String json = WorkersUtils.getFileContent(file.getAbsolutePath());
                Queue<DataPacket> queue = WorkersUtils.fromJSON(new TypeReference<>() {
                }, json);
                while (!queue.isEmpty()) {
                    monitorWebClient.sendPacket(queue.poll());
                    log.trace("Packet was sent");
                }
                file.delete();
            });
        }
    }

    private DataPacket createPacket() {
        isLocked = true;
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DataPacket dataPacket = new DataPacket(agentId, date.getTime(), dataPacketEntries);
        isLocked = false;
        return dataPacket;
    }

    public synchronized void addMonitoredParameterValue(Timestamp timestamp, String monitoredValue, UUID serviceId, UUID parameterId) {
        MonitoredParameterEntry entry = new MonitoredParameterEntry(timestamp, monitoredValue);
        while(isLocked){
            try {
                Thread.sleep(AppConstants.WAIT_WHEN_IS_LOCKED_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (dataPacketEntries.containsKey(serviceId)) {
            dataPacketEntries.get(serviceId).addParameter(parameterId, entry);
        } else {
            dataPacketEntries.put(serviceId, new ServiceDataEntries());
        }
    }

    public synchronized void addLog(UUID serviceId, String path, Timestamp timestamp, String line) {
        LogEntry entry = new LogEntry(timestamp, line);
        while(isLocked){
            try {
                Thread.sleep(AppConstants.WAIT_WHEN_IS_LOCKED_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (dataPacketEntries.containsKey(serviceId)) {
            dataPacketEntries.get(serviceId).addLog(path, entry);
        } else {
            dataPacketEntries.put(serviceId, new ServiceDataEntries());
        }
    }

    public void onExit() {
        File temporaryFolder = new File(temporaryPath);
        DataPacket dataPacket = createPacket();
        packetQueue.add(dataPacket);
        savePacketsToFiles(temporaryFolder);
    }
}

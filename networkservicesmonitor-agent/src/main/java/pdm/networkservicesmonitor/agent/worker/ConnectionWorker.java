package pdm.networkservicesmonitor.agent.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.AppConstants;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.connection.MonitorWebClient;
import pdm.networkservicesmonitor.agent.payloads.data.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Stream;

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
    private List<ServiceLogEntries> serviceLogEntries;

    @Getter
    private List<ServiceMonitoringParametersEntries> serviceMonitoredParametersEntries;

    private Date date;

    // TODO(high): Proxy packages: send packet to proxy(with OK resp) and remove all info about packet if OK
    public ConnectionWorker() {
        date = new Date();
        isLocked = false;
        packetQueue = new ArrayBlockingQueue<>(AppConstants.MAX_PACKETS_IN_SENDING_QUEUE);
        serviceLogEntries = new ArrayList<>();
        serviceMonitoredParametersEntries = new ArrayList<>();
    }

    private static <T> T fromJSON(final TypeReference<T> type, final String jsonPacket) {
        T data = null;
        try {
            data = new ObjectMapper().readValue(jsonPacket, type);
        } catch (Exception e) {
            log.error("Cannot load json content from file");
            log.error(e.getMessage());
        }
        return data;
    }

    private static String getFileContent(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            log.error("Cannot open file " + filePath);
            log.error(e.getMessage());
        }

        return contentBuilder.toString();
    }

    public void addNewServiceLogEntries(ServiceLogEntries logEntries) {
        serviceLogEntries.add(logEntries);
    }

    public void addNewServiceMonitoredParametersEntries(ServiceMonitoringParametersEntries serviceMonitoringParametersEntries) {
        serviceMonitoredParametersEntries.add(serviceMonitoringParametersEntries);
    }

    // TODO(critical): double check and catching exceptions to avoid crashing threads
    @Override
    public void run() {
        ObjectMapper objectMapper = new ObjectMapper();
        File temporaryFolder = new File(temporaryPath);

        while (true) {
            try {
                Thread.sleep(agentConfigurationManager.getAgentConfiguration().getSendingInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isLocked = true;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            DataPacket dataPacket = new DataPacket(agentId, date.getTime(), serviceLogEntries, serviceMonitoredParametersEntries);
            serviceLogEntries.forEach(sle -> sle.setLogs(new ArrayList<>()));
            serviceMonitoredParametersEntries.forEach(smpe -> smpe.setMonitoredParameters(new ArrayList<>()));

            isLocked = false;

            try {
                //TODO(low): check connection before reading file content
                //TODO(medium): test fix below and test it
                File[] listOfFiles = temporaryFolder.listFiles((dir, name) -> name.matches("packet.*"));
                if (listOfFiles != null) {
                    Arrays.stream(listOfFiles).forEach(file -> {
                        log.debug("Reading content form file: " + file.getAbsolutePath());
                        String json = getFileContent(file.getAbsolutePath());
                        Queue<DataPacket> queue = fromJSON(new TypeReference<Queue<DataPacket>>() {
                        }, json);
                        while (!queue.isEmpty()) {
                            monitorWebClient.sendPacket(queue.poll());
                            log.trace("Packet sent");
                        }
                        file.delete();
                    });
                }

                while (!packetQueue.isEmpty()) {
                    monitorWebClient.sendPacket(packetQueue.peek());
                    packetQueue.poll();
                    log.trace("Packet sent");
                }
                monitorWebClient.sendPacket(dataPacket);

                log.trace("All packets sent");

            } catch (Exception e) {
                log.error("Packet cannot be send due to connection problems");
                log.error(e.getMessage());
                e.printStackTrace();
                if (packetQueue.size() + 1 >= AppConstants.MAX_PACKETS_IN_SENDING_QUEUE) {
                    packetQueue.add(dataPacket);
                    try {
                        //TODO(low): create file if not exists
                        //TODO(low): prevent Queue full state when problems with saving occurs
                        // TODO(medium): add sth to save all collected data to file when SIGINT received
                        String fileName = String.format("%s/packets_%s.dat", temporaryPath, UUID.randomUUID().toString());
                        objectMapper.writeValue(new File(fileName), packetQueue);
                        log.info(String.format("All %d packets queue entries saved to files. Packets will be loaded and send when connection will be opened", packetQueue.size()));
                        packetQueue.clear();
                    } catch (IOException ex) {
                        log.error("Cannot save packet data too file when cleaning packet queue when queue full");
                        log.error(ex.getMessage());
                    }
                } else {
                    packetQueue.add(dataPacket);
                }

            }

        }

    }

    public void addLog(String logValue, Timestamp timestamp, int ordinal) {
        while (isLocked()) {
            log.trace("Waiting... Log will be added when adding will be unlocked");
            try {
                Thread.sleep(AppConstants.WAIT_WHEN_IS_LOCKED_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serviceLogEntries.get(ordinal).getLogs().add(new LogEntry(timestamp, logValue));
    }

    public void addMonitoredParameterValue(String parameterValue, Timestamp timestamp, int ordinal) {
        while (isLocked()) {
            log.trace("Waiting... Monitored Parameter Value will be added when adding will be unlocked");
            try {
                Thread.sleep(AppConstants.WAIT_WHEN_IS_LOCKED_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serviceMonitoredParametersEntries.get(ordinal).getMonitoredParameters().add(new MonitoredParameterEntry(timestamp, parameterValue));
    }
}

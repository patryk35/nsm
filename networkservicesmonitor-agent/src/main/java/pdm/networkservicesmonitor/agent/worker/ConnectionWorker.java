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

    @Getter
    private List<LogEntry> internalProblemsLogs;

    private Date date;

    public ConnectionWorker() {
        date = new Date();
        isLocked = false;
        packetQueue = new ArrayBlockingQueue<>(AppConstants.MAX_PACKETS_IN_SENDING_QUEUE);
        serviceLogEntries = new ArrayList<>();
        serviceMonitoredParametersEntries = new ArrayList<>();
        internalProblemsLogs = new ArrayList<>();
    }

    @Override
    public void run() {
        ObjectMapper objectMapper = new ObjectMapper();
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
            try {
                Thread.sleep(agentConfigurationManager.getAgentConfiguration().getSendingInterval());
            } catch (InterruptedException e) {
                addAgentInternalProblemLog(e.getMessage(), new Timestamp(System.currentTimeMillis()));
                e.printStackTrace();
            }
            isLocked = true;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            DataPacket dataPacket = new DataPacket(agentId, date.getTime(), serviceLogEntries, serviceMonitoredParametersEntries);
            serviceLogEntries.forEach(s -> s.setLogs(new ArrayList<>()));
            serviceMonitoredParametersEntries.forEach(s -> s.setMonitoredParameters(new ArrayList<>()));

            isLocked = false;

            try {
                //TODO(medium): each packet should have id - monitor checks id, if packet was received before it skip this packet
                //TODO(low): Additional worker to send logs from temp directory - in other case it will resend files, but packet in creation can be really huge in meantime
                //TODO(medium): test fix below and test it
                monitorWebClient.testMonitorConnection();

                File[] listOfFiles = temporaryFolder.listFiles((dir, name) -> name.matches("packet.*"));
                if (listOfFiles != null) {
                    Arrays.stream(listOfFiles).forEach(file -> {
                        log.debug("Reading content form file: " + file.getAbsolutePath());
                        String json = getFileContent(file.getAbsolutePath());
                        Queue<DataPacket> queue = fromJSON(new TypeReference<>() {
                        }, json);
                        while (!queue.isEmpty()) {
                            monitorWebClient.sendPacket(queue.poll());
                            log.trace("Packet was sent");
                        }
                        file.delete();
                    });
                }

                while (!packetQueue.isEmpty()) {
                    monitorWebClient.sendPacket(packetQueue.peek());
                    packetQueue.poll();
                    log.trace("Packet was sent");
                }
                monitorWebClient.sendPacket(dataPacket);

                log.trace("All packets sent");

            } catch (Exception e) {
                log.error("Packet cannot be send due to connection problems");
                log.error(e.getMessage());

                addAgentInternalProblemLog("Packet cannot be send due to connection problems", new Timestamp(System.currentTimeMillis()));
                addAgentInternalProblemLog(e.getMessage(), new Timestamp(System.currentTimeMillis()));

                if (packetQueue.size() >= AppConstants.MAX_PACKETS_IN_SENDING_QUEUE) {
                    try {
                        //TODO(low): create file if not exists
                        //TODO(low): prevent Queue full state when problems with saving occurs
                        // TODO(medium): add sth to save all collected data to file when SIGINT received
                        if (!temporaryFolder.exists()) {
                            try {
                                log.warn(String.format("Temporary directory %s was removed. Creating new one.", temporaryFolder.getAbsolutePath()));
                                addAgentInternalProblemLog("Temporary directory %s was removed. Creating new one.", new Timestamp(System.currentTimeMillis()));
                                Files.createDirectories(temporaryFolder.toPath());
                            } catch (IOException ex) {
                                log.error("Critical error, application will exit. Cannot create temporary directory. Make sure that application has access to path " + temporaryFolder.getAbsolutePath());
                                log.error(ex.getMessage());
                                // Without this directory agent cannot work properly
                                System.exit(-10404);
                            }
                        }
                        String fileName = String.format("%s/packets_%s.dat", temporaryPath, UUID.randomUUID().toString());
                        objectMapper.writeValue(new File(fileName), packetQueue);
                        log.info(String.format("All %d packets queue entries saved to files. Packets will be loaded and send when connection will be opened", packetQueue.size()));
                        packetQueue.clear();
                    } catch (IOException ex) {
                        log.error("Cannot save packet data too file when cleaning packet queue when queue full");
                        log.error(ex.getMessage());
                    }
                }
                packetQueue.add(dataPacket);

            }

        }

    }

    public void addServiceLogsEntries(ServiceLogEntries logEntries) {
        serviceLogEntries.add(logEntries);
    }

    public void addServiceMonitoredParametersEntries(ServiceMonitoringParametersEntries serviceMonitoringParametersEntries) {
        serviceMonitoredParametersEntries.add(serviceMonitoringParametersEntries);
    }

    //TODO: agent send to monitor all problems ( agent logs section in agent tab in client)
    // TODO(medium): use it, add to package and save in monitor to db
    // TODO(critical): Do it in other wasy - mayby monitoring log file - all entries are send to moitor and in client all logs can be viewed - alerts for errors.
    // Logs verification - after start it gets last saved error and send all newer logs
    // Use sending logs like for Plunk
    public void addAgentInternalProblemLog(String logValue, Timestamp timestamp) {
        internalProblemsLogs.add(new LogEntry(timestamp, logValue));
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

    // TODO(low): move to utils
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

    // TODO(low): move to utils
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
}

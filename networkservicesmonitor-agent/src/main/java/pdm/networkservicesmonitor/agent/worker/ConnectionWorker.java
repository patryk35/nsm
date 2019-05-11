package pdm.networkservicesmonitor.agent.worker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.AppConstants;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.connection.MonitorWebClient;
import pdm.networkservicesmonitor.agent.payloads.data.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

@Component
@Slf4j
public class ConnectionWorker implements Runnable {

    @Value("${agent.id}")
    private UUID agentId;

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


    public ConnectionWorker() {
        date = new Date();
        isLocked = false;
        packetQueue = new ArrayBlockingQueue<>(AppConstants.MAX_PACKETS_IN_SENDING_QUEUE);
        serviceLogEntries = new ArrayList<>();
        serviceMonitoredParametersEntries = new ArrayList<>();
    }

    public void addNewServiceLogEntries(ServiceLogEntries logEntries) {
        serviceLogEntries.add(logEntries);
    }

    public void addNewServiceMonitoredParametersEntries(ServiceMonitoringParametersEntries serviceMonitoringParametersEntries) {
        serviceMonitoredParametersEntries.add(serviceMonitoringParametersEntries);
    }

    @Override
    public void run() {

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

            //dataPacket.addLogs(serviceLogEntries);

            serviceLogEntries.forEach(sle -> {
                sle.setLogs(new ArrayList<>());
            });
            serviceMonitoredParametersEntries.forEach(smpe -> {
                smpe.setMonitoredParameters(new ArrayList<>());
            });
            isLocked = false;
            try {
                while (!packetQueue.isEmpty()) {
                    monitorWebClient.sendPacket(packetQueue.peek());
                }
                monitorWebClient.sendPacket(dataPacket);

            } catch (Exception e) {
                // TODO: 2 options for proxy packages: send packet to proxy(with OK resp) and remove all info about packet
                // Second option - create additional Queue/List (with limi of course) to keep this packets and wait for resp from monitor
                log.error("Packet cannot be send due to connection problems");
                log.error(e.getMessage());
                if (packetQueue.size() == 20) {
                    //TODO: save to file all 21 collected packets
                    // below temporary option
                    Queue<DataPacket> dataPackets = new ArrayBlockingQueue<>(2 * packetQueue.size());
                    dataPackets.addAll(packetQueue);
                    dataPackets.add(dataPacket);
                    log.error("Queue length duplicated");
                } else {
                    packetQueue.add(dataPacket);
                }
                // TODO: in case of opening connection again, load packages from file and resend

            }
            log.trace("Log sended");

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

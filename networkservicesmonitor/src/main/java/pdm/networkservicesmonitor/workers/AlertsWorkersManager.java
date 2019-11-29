package pdm.networkservicesmonitor.workers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.model.agent.AgentConfiguration;
import pdm.networkservicesmonitor.model.agent.Packet;
import pdm.networkservicesmonitor.model.alert.AlertStatus;
import pdm.networkservicesmonitor.repository.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component("alertsWorkersManager")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AlertsWorkersManager extends Thread {

    @Autowired
    private ApplicationContext appContext;

    @Value("${app.alerts.workers.count}")
    private int workersCount;

    @Value("${app.alerts.check.interval}")
    private int sleepTime;

    @Autowired
    private CollectedLogsRepository collectedLogsRepository;

    @Autowired
    private AlertStatusRepository alertStatusRepository;

    @Autowired
    private MonitoredParametersValuesRepository monitoredParametersValuesRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PacketRepository packetRepository;

    @Override
    public void run() {
        Optional<AlertStatus> logsAlertsStatusOptional = alertStatusRepository.findAllByName("logs");
        Optional<AlertStatus> monitoringAlertsStatusOptional = alertStatusRepository.findAllByName("monitoring");

        AlertStatus logsAlertsStatus = logsAlertsStatusOptional
                .orElseGet(() -> new AlertStatus("logs", 0l));
        AlertStatus monitoringAlertsStatus = monitoringAlertsStatusOptional
                .orElseGet(() -> new AlertStatus("monitoring", 0l));


        while (true) {
            long currentLogsId = collectedLogsRepository.getLastId();
            long currentMonitoringId = monitoredParametersValuesRepository.getLastId();

            try {
                if (currentLogsId != logsAlertsStatus.getLastId()) {
                    LogsAlertsWorker logsWorker = appContext.getBean("logsAlertsWorker", LogsAlertsWorker.class);
                    logsWorker.setup(logsAlertsStatus.getLastId() + 1, currentLogsId);
                    logsWorker.run();
                }

                if (currentMonitoringId != monitoringAlertsStatus.getLastId()) {
                    MonitoringAlertsWorker monitoringAlertsWorker = appContext
                            .getBean("monitoringAlertsWorker", MonitoringAlertsWorker.class);
                    monitoringAlertsWorker.setup(monitoringAlertsStatus.getLastId() + 1, currentMonitoringId);
                    monitoringAlertsWorker.run();
                }

                agentRepository.findAllByIsDeleted(false).parallelStream().forEach(agent -> {
                    boolean status;
                    Optional<Packet> lastPacket = packetRepository.findLastByAgentId(agent.getId());
                    AgentConfiguration configuration = agent.getAgentConfiguration();
                    if(lastPacket.isPresent()){
                        Packet packet = lastPacket.get();
                        long currentTime = (new Date()).getTime();
                        Timestamp alertLevelTime = new Timestamp(currentTime + (5 * configuration.getSendingInterval())); // TODO(medium): Change 5 to choose value in agent configuration
                        if((packet.getReceivingTimestamp()).compareTo(alertLevelTime) < 0 ){
                            status = false;
                        } else {
                            status = true;
                        }
                    } else {
                        status = false;
                    }
                    if(status != agent.isConnected()){
                        agent.setConnected(status);
                        agentRepository.save(agent);
                    }
                });
            } catch (Exception e) {
                log.error("Problems occurred during collecting alerts");
                log.error(e.getMessage());
                e.printStackTrace();
            }

            logsAlertsStatus.setLastId(currentLogsId);
            monitoringAlertsStatus.setLastId(currentMonitoringId);
            alertStatusRepository.save(logsAlertsStatus);
            alertStatusRepository.save(monitoringAlertsStatus);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
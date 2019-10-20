package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.model.alert.AlertStatus;
import pdm.networkservicesmonitor.repository.AlertStatusRepository;
import pdm.networkservicesmonitor.repository.CollectedLogsRepository;
import pdm.networkservicesmonitor.repository.MonitoredParametersValuesRepository;

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


    @Override
    public void run() {
        Optional<AlertStatus> logsAlertsStatusOptional = alertStatusRepository.findAllByName("logs");
        Optional<AlertStatus> monitoringAlertsStatusOptional = alertStatusRepository.findAllByName("monitoring");

        AlertStatus logsAlertsStatus = logsAlertsStatusOptional
                .orElseGet(() -> new AlertStatus("logs", 0l));
        AlertStatus monitoringAlertsStatus = monitoringAlertsStatusOptional
                .orElseGet(() -> new AlertStatus("monitoring", 0l));


        /*while (true) {
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
            } catch (Exception e) {
                log.error("Problems occurred during collecting alerts");
                log.error(e.getMessage());
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
        }*/
    }
}

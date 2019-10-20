package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.model.alert.LogsAlertConfiguration;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.model.data.LogsAlert;
import pdm.networkservicesmonitor.repository.CollectedLogsRepository;
import pdm.networkservicesmonitor.repository.LogsAlertsConfigurationRepository;
import pdm.networkservicesmonitor.repository.LogsAlertsRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("logsAlertsWorker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LogsAlertsWorker extends Thread{

    @Autowired
    private LogsAlertsConfigurationRepository configurationRepository;
    @Autowired
    private CollectedLogsRepository logsRepository;
    @Autowired
    private LogsAlertsRepository logsAlertsRepository;

    private Long start;
    private Long end;

    public void setup(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public void run() {
        ArrayList<LogsAlertConfiguration> configurations = configurationRepository.findByEnabled(true);
        configurations.parallelStream().forEach((conf) -> {
            List<CollectedLog> matchingLogs = logsRepository.findByAlertConfiguration(
                    conf.getService().getId(),
                    conf.getSearchString(),
                    conf.getPathSearchString(),
                    start,
                    end
            );
            matchingLogs.parallelStream().forEach(l -> {
                LogsAlert alert = new LogsAlert(conf, l);
                logsAlertsRepository.save(alert);
            });
        });
    }
}

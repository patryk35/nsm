package pdm.networkservicesmonitor.workers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.model.alert.LogsAlertConfiguration;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.model.data.LogsAlert;
import pdm.networkservicesmonitor.repository.CollectedLogsRepository;
import pdm.networkservicesmonitor.repository.LogsAlertsConfigurationRepository;
import pdm.networkservicesmonitor.repository.LogsAlertsRepository;
import pdm.networkservicesmonitor.service.MailingService;

import java.util.ArrayList;
import java.util.List;

import static pdm.networkservicesmonitor.workers.WorkersUtils.translateAlertLevel;

@Slf4j
@Component("logsAlertsWorker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LogsAlertsWorker extends Thread {

    @Autowired
    private LogsAlertsConfigurationRepository configurationRepository;
    @Autowired
    private CollectedLogsRepository logsRepository;
    @Autowired
    private LogsAlertsRepository logsAlertsRepository;

    @Autowired
    @Qualifier("alertMailContentString")
    private String alertMailContentString;

    @Autowired
    private MailingService mailingService;

    private Long start;
    private Long end;

    public void setup(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public void run() {
        ArrayList<LogsAlertConfiguration> configurations = configurationRepository.findByEnabled(true);
        configurations.stream().forEach((conf) -> {
            List<CollectedLog> matchingLogs = logsRepository.findByAlertConfiguration(
                    conf.getService().getId(),
                    conf.getSearchString(),
                    conf.getPathSearchString(),
                    start,
                    end
            );

            matchingLogs.stream().forEach(l -> {
                LogsAlert alert = new LogsAlert(conf, l);
                logsAlertsRepository.save(alert);
                if (conf.isEmailNotification()) {
                    String content = alertMailContentString
                            .replace("%level%", translateAlertLevel(conf.getAlertLevel()))
                            .replace("%time%", l.getTimestamp().toString())
                            .replace("%message%", conf.getMessage())
                            .replace("%agent%", l.getService().getAgent().getName())
                            .replace("%service%", l.getService().getName())
                            .replace("%additional%", "");
                    conf.getRecipients().forEach(r -> {
                        mailingService.sendMail(r, String.format("Nowy alert dotyczący %s/%s ( poziom %s )",
                                l.getService().getAgent().getName(),
                                l.getService().getName(),
                                translateAlertLevel(conf.getAlertLevel())),
                                content
                        );
                    });
                }
            });
        });
    }
}
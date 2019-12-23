package pdm.networkservicesmonitor.workers;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pdm.networkservicesmonitor.model.alert.MonitoringAlertConfiguration;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;
import pdm.networkservicesmonitor.model.data.MonitoringAlert;
import pdm.networkservicesmonitor.repository.MonitoredParametersValuesRepository;
import pdm.networkservicesmonitor.repository.MonitoringAlertsConfigurationRepository;
import pdm.networkservicesmonitor.repository.MonitoringAlertsRepository;
import pdm.networkservicesmonitor.service.MailingService;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static pdm.networkservicesmonitor.workers.WorkersUtils.translateAlertLevel;

@Slf4j
@Component("monitoringAlertsWorker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Transactional
public class MonitoringAlertsWorker extends Thread {
    @Autowired
    private MonitoringAlertsConfigurationRepository configurationRepository;
    @Autowired
    private MonitoredParametersValuesRepository monitoringRepository;
    @Autowired
    private MonitoringAlertsRepository monitoringAlertsRepository;
    @Autowired
    private EntityManager entityManager;
    private Long start;
    private Long end;

    @Autowired
    @Qualifier("alertMailContentString")
    private String alertMailContentString;

    @Autowired
    private MailingService mailingService;

    public void setup(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public void run() {
        Session session = entityManager.unwrap(Session.class);
        ArrayList<MonitoringAlertConfiguration> configurations = configurationRepository.findByEnabled(true);
        configurations.parallelStream().forEach((conf) -> {
            double multiplier;
            if(conf.getMonitoredParameterType().getUnit().equals("%")){
                multiplier=100;
            } else {
                multiplier = conf.getMonitoredParameterType().getMultiplier();
            }
            Query query = session.createQuery(String.format(
                    "select l from collected_parameters_values l where service_id = '%s' AND parameter_type_id = '%s' " +
                            "AND value %s %s AND id >= %d AND id <= %d",
                    conf.getService().getId().toString(),
                    conf.getMonitoredParameterType().getId().toString(),
                    conf.getCondition(),
                    conf.getValue()/multiplier,
                    start,
                    end)
            );

            query.getResultList().parallelStream().forEach(m -> {
                MonitoringAlert alert = new MonitoringAlert(conf, (MonitoredParameterValue) m);
                monitoringAlertsRepository.save(alert);
                if (conf.isEmailNotification()) {
                    String content = alertMailContentString
                            .replace("%level%", conf.getAlertLevel().toString())
                            .replace("%time%", ((MonitoredParameterValue) m).getTimestamp().toString())
                            .replace("%message%", conf.getMessage())
                            .replace("%agent%", ((MonitoredParameterValue) m).getService().getAgent().getName())
                            .replace("%service%", ((MonitoredParameterValue) m).getService().getName())
                            .replace("%additional%", String.format("Dla parametru: %s", conf.getMonitoredParameterType().getName()));
                    conf.getRecipients().forEach(r -> {
                        mailingService.sendMail(r, String.format("Nowy alert dotyczący %s/%s [ parametr %s, poziom %s ]",
                                ((MonitoredParameterValue) m).getService().getAgent().getName(),
                                ((MonitoredParameterValue) m).getService().getName(),
                                ((MonitoredParameterValue) m).getParameterType().getName(),
                                translateAlertLevel(conf.getAlertLevel())),
                                content
                        );
                    });
                }
            });
        });
    }
}

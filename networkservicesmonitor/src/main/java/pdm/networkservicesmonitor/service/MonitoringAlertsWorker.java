package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;

@Slf4j
@Component("monitoringAlertsWorker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Transactional
public class MonitoringAlertsWorker extends Thread{
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

    public void setup(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public void run() {
        // TODO(major): both workers should sort ASC results to keep order
        // TODO: Comparing variables is working, but it tricky. Variables are always sting values !!! Consider rewriting it
        Session session = entityManager.unwrap(Session.class);
        ArrayList<MonitoringAlertConfiguration> configurations = configurationRepository.findByEnabled(true);
        configurations.parallelStream().forEach((conf) -> {
            Query query = session.createQuery(String.format(
                    "select l from parameters l where service_id = '%s' AND parameter_type_id = '%s' " +
                            "AND value %s '%s' AND id >= %d AND id <= %d",
                    conf.getService().getId().toString(),
                    conf.getMonitoredParameterType().getId().toString(),
                    conf.getCondition(),
                    conf.getValue(),
                    start,
                    end)
            );
            query.getResultList().parallelStream().forEach(m -> {
                MonitoringAlert alert = new MonitoringAlert(conf, (MonitoredParameterValue)m);
                monitoringAlertsRepository.save(alert);
            });
        });
    }
}

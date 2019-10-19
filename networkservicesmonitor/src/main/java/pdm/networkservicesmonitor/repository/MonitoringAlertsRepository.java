package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.data.MonitoringAlert;

@RepositoryRestResource(exported = false)
public interface MonitoringAlertsRepository extends JpaRepository<MonitoringAlert, Long> {

}

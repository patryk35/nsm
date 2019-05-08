package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;

@RepositoryRestResource(exported = false)
public interface MonitoredParametersValuesRepository extends JpaRepository<MonitoredParameterValue, Long> {
}

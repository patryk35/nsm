package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.alert.LogsAlertConfiguration;
import pdm.networkservicesmonitor.model.alert.MonitoringAlertConfiguration;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface MonitoringAlertsConfigurationRepository extends JpaRepository<MonitoringAlertConfiguration, UUID> {
    ArrayList<MonitoringAlertConfiguration> findByEnabled(boolean enabled);
    Optional<MonitoringAlertConfiguration> findByIdAndDeleted(UUID id, boolean deleted);
    Page<MonitoringAlertConfiguration> findByDeleted(boolean deleted, Pageable pageable);
}

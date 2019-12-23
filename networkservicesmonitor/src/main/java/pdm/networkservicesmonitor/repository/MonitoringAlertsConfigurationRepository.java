package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.alert.MonitoringAlertConfiguration;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public interface MonitoringAlertsConfigurationRepository extends JpaRepository<MonitoringAlertConfiguration, UUID> {
    ArrayList<MonitoringAlertConfiguration> findByEnabled(boolean enabled);

    Optional<MonitoringAlertConfiguration> findByIdAndDeleted(UUID id, boolean deleted);

    Page<MonitoringAlertConfiguration> findByDeleted(boolean deleted, Pageable pageable);
}

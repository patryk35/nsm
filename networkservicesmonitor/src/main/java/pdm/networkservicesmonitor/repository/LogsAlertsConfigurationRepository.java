package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.alert.LogsAlertConfiguration;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public interface LogsAlertsConfigurationRepository extends JpaRepository<LogsAlertConfiguration, UUID> {
    ArrayList<LogsAlertConfiguration> findByEnabled(boolean enabled);
    Optional<LogsAlertConfiguration> findByIdAndDeleted(UUID id, boolean deleted);
    Page<LogsAlertConfiguration> findByDeleted(boolean deleted, Pageable pageable);
}

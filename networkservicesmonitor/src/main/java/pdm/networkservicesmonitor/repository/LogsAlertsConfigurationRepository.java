package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.alert.LogsAlertConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface LogsAlertsConfigurationRepository extends JpaRepository<LogsAlertConfiguration, UUID> {
    ArrayList<LogsAlertConfiguration> findByEnabled(boolean enabled);
    Optional<LogsAlertConfiguration> findByIdAndDeleted(UUID id, boolean deleted);
    Page<LogsAlertConfiguration> findByDeleted(boolean deleted, Pageable pageable);
}

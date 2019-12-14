package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.service.LogsCollectingConfiguration;

import java.util.UUID;

public interface LogsCollectingConfigurationRepository extends JpaRepository<LogsCollectingConfiguration, UUID> {
    Page<LogsCollectingConfiguration> findByServiceIdAndIsDeleted(UUID serviceId, boolean isDeleted, Pageable pageable);

}

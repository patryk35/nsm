package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import pdm.networkservicesmonitor.model.AgentSettings;
import pdm.networkservicesmonitor.model.MonitorAgent;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
@Repository
public interface AgentSettingsRepository extends JpaRepository<AgentSettings, Long> {
    Optional<AgentSettings> findById(Long id);


}
package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;

@RepositoryRestResource(exported = false)
public interface LogsCollectingConfigurationRepository extends JpaRepository<LogsCollectingConfiguration, Long> {
}

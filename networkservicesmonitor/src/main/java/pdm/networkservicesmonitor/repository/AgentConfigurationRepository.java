package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.agent.AgentConfiguration;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface AgentConfigurationRepository extends JpaRepository<AgentConfiguration, Long> {
    Optional<AgentConfiguration> findById(Long id);
}
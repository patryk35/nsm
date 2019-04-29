package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import pdm.networkservicesmonitor.model.agent.configuration.AgentConfiguration;

import java.util.Optional;

@RepositoryRestResource(exported = false)
@Repository
public interface AgentConfigurationRepository extends JpaRepository<AgentConfiguration, Long> {
    Optional<AgentConfiguration> findById(Long id);


}
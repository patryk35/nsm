package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.agent.AgentConfiguration;

import java.util.Optional;

public interface AgentConfigurationRepository extends JpaRepository<AgentConfiguration, Long> {
    Optional<AgentConfiguration> findById(Long id);
}
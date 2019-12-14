package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.data.AgentError;

public interface AgentErrorRepository extends JpaRepository<AgentError, Long> {
}

package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface AgentRepository extends JpaRepository<MonitorAgent, UUID> {
    Optional<MonitorAgent> findById(UUID agentId);

    // TODO: It find only first one agent, do checking getting agents
    Optional<MonitorAgent> findByName(String name);

    Page<MonitorAgent> findByIsDeleted(boolean isDeleted, Pageable pageable);

    boolean existsByName(String name);
}
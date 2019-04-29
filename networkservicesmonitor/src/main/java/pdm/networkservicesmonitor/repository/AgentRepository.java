package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
@Repository
public interface AgentRepository extends JpaRepository<MonitorAgent, UUID> {
    Optional<MonitorAgent> findById(UUID agentId);

    Page<MonitorAgent> findAll(Pageable pageable);

}
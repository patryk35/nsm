package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentRepository extends JpaRepository<MonitorAgent, UUID> {
    Optional<MonitorAgent> findById(UUID agentId);

    //TODO(major): change it to list -> eg. because of deleted
    Optional<MonitorAgent> findByName(String name);

    Optional<MonitorAgent> findByNameAndIsDeleted(String name, boolean isDeleted);


    Page<MonitorAgent> findByIsDeleted(boolean isDeleted, Pageable pageable);

    boolean existsByNameAndIsDeleted(String name, boolean isDeleted);

    List<MonitorAgent> findAllByIsDeleted(boolean b);
}
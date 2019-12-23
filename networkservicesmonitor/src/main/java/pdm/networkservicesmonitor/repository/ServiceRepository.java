package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.service.Service;

import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Optional<Service> findByAgentIdAndNameAndIsDeleted(UUID agentId, String name, boolean isDeleted);

    Page<Service> findByAgentIdAndIsDeleted(UUID agentId, boolean isDeleted, Pageable pageable);

    boolean existsByNameAndAgentIdAndIsDeleted(String name, UUID agentId, boolean isDeleted);
}

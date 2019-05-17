package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.agent.service.Service;

import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {
    // TODO: It find only first one agent, do checking getting agents
    Optional<Service> findByName(String name);

}

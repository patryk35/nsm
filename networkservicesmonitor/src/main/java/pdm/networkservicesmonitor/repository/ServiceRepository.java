package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.agent.service.Service;

import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {
}

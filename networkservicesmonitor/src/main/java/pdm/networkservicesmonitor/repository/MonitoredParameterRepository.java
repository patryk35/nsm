package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import pdm.networkservicesmonitor.model.MonitoredParameter;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
@Repository
public interface MonitoredParameterRepository extends JpaRepository<MonitoredParameter, UUID> {
    Optional<MonitoredParameter> findById(UUID id);

}

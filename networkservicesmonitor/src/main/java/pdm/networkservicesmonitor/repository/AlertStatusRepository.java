package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.alert.AlertStatus;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface AlertStatusRepository extends JpaRepository<AlertStatus, String> {
    public Optional<AlertStatus> findAllByName(String name);
}

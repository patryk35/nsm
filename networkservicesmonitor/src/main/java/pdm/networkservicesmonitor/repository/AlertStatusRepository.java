package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.alert.AlertStatus;

import java.util.Optional;

public interface AlertStatusRepository extends JpaRepository<AlertStatus, String> {
    Optional<AlertStatus> findAllByName(String name);
}

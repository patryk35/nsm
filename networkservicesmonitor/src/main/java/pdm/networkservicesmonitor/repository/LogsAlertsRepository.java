package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.data.LogsAlert;

public interface LogsAlertsRepository extends JpaRepository<LogsAlert, Long> {
    Page<LogsAlert> findAll(Pageable pageable);
}

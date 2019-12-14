package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.data.MonitoringAlert;

public interface MonitoringAlertsRepository extends JpaRepository<MonitoringAlert, Long> {
    Page<MonitoringAlert> findAll(Pageable pageable);
}

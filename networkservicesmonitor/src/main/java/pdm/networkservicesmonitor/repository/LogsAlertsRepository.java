package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.data.LogsAlert;

import java.util.ArrayList;

@RepositoryRestResource(exported = false)
public interface LogsAlertsRepository extends JpaRepository<LogsAlert,Long> {
    Page<LogsAlert> findAll(Pageable pageable);
}

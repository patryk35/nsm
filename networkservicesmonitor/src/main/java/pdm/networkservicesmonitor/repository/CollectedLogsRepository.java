package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.data.CollectedLog;

import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface CollectedLogsRepository  extends JpaRepository<CollectedLog, UUID> {
}

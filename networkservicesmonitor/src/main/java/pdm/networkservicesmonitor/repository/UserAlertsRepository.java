package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.data.UserAlert;

@RepositoryRestResource(exported = false)
public interface UserAlertsRepository extends JpaRepository<UserAlert, Long> {
}

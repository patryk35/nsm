package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.data.UserAlert;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface UserAlertsRepository extends JpaRepository<UserAlert, Long> {
    Page<UserAlert> findAll(Pageable pageable);
    Optional<UserAlert> findById(Long id);

}

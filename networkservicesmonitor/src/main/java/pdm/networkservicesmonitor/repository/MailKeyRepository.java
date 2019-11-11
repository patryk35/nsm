package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.user.MailKeyType;
import pdm.networkservicesmonitor.model.user.MailKey;
import pdm.networkservicesmonitor.model.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailKeyRepository extends JpaRepository<MailKey, UUID> {
    public List<MailKey> findAllByUserAndType(User user, MailKeyType type);
    Optional<MailKey> findByIdAndType(UUID id, MailKeyType type);
}

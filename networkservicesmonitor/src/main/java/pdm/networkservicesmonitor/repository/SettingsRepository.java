package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.data.Settings;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, String> {
    Optional<Settings> findByKey(String key);
}

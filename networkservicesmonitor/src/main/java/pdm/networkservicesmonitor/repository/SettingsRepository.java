package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.data.Settings;

public interface SettingsRepository extends JpaRepository<Settings, String> {
}

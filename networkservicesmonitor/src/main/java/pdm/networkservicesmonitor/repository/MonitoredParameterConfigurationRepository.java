package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.service.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.model.service.Service;

import java.util.List;
import java.util.UUID;

public interface MonitoredParameterConfigurationRepository extends JpaRepository<MonitoredParameterConfiguration, UUID> {
    Page<MonitoredParameterConfiguration> findByServiceIdAndIsDeleted(UUID serviceId, boolean isDeleted, Pageable pageable);

    List<MonitoredParameterConfiguration> findByServiceAndIsDeleted(Service service, boolean isDeleted);
}

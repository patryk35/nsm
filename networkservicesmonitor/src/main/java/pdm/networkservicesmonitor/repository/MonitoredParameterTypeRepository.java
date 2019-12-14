package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdm.networkservicesmonitor.model.service.MonitoredParameterType;

import java.util.List;
import java.util.UUID;

public interface MonitoredParameterTypeRepository extends JpaRepository<MonitoredParameterType, UUID> {
    List<MonitoredParameterType> findAllByParentId(UUID parentId);
}

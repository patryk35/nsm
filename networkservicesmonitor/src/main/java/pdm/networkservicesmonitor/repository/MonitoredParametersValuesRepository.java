package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface MonitoredParametersValuesRepository extends JpaRepository<MonitoredParameterValue, Long> {
    @Query("select l from collected_parameters_values l where service_id in :ids AND parameter_type_id = :paramId")
    List<MonitoredParameterValue> findByServiceIdsAAndParameterTypeId(@Param("ids") List<UUID> servicesIds,
                                                                      @Param("paramId") UUID parametersIds);

    @Query("select l from collected_parameters_values l where service_id in :ids AND parameter_type_id = :paramId and timestamp >= :timestampFrom")
    List<MonitoredParameterValue> findByServiceIdsAAndParameterTypeIdWithFromTimestamp(@Param("ids") List<UUID> servicesIds,
                                                                                       @Param("paramId") UUID parametersId,
                                                                                       @Param("timestampFrom") Timestamp timestampFrom);

    @Query("select l from collected_parameters_values l where service_id in :ids AND parameter_type_id = :paramId and timestamp <= :timestampTo")
    List<MonitoredParameterValue> findByServiceIdsAAndParameterTypeIdWithToTimestamp(@Param("ids") List<UUID> servicesIds,
                                                                                     @Param("paramId") UUID parametersId,
                                                                                     @Param("timestampTo") Timestamp timestampTo);

    @Query("select l from collected_parameters_values l where service_id in :ids AND parameter_type_id = :paramId and timestamp >= :timestampFrom and timestamp <= :timestampTo")
    List<MonitoredParameterValue> findByServiceIdsAAndParameterTypeIdWithTimestamp(@Param("ids") List<UUID> servicesIds,
                                                                                   @Param("paramId") UUID parametersId,
                                                                                   @Param("timestampFrom") Timestamp timestampFrom,
                                                                                   @Param("timestampTo") Timestamp timestampTo);

    @Query("SELECT MAX(id) from collected_parameters_values")
    long getLastId();
}

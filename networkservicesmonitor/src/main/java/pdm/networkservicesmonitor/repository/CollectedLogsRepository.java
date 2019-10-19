package pdm.networkservicesmonitor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.data.CollectedLog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface CollectedLogsRepository extends JpaRepository<CollectedLog, UUID> {
    @Query("select l from logs l where service_id in :ids AND path LIKE %:path% AND log like %:searchString%")
    Page<CollectedLog> findByServiceIds(Pageable pageable, @Param("ids") List<UUID> servicesIds,
                                        @Param("path") String path, @Param("searchString") String searchString);

    @Query("select l from logs l where service_id in :ids and timestamp >= :timestampFrom AND path LIKE %:path% AND log like %:searchString%")
    Page<CollectedLog> findByServiceIdsWithFromTimestamp(Pageable pageable, @Param("ids") List<UUID> servicesIds,
                                                         @Param("timestampFrom") Timestamp timestamp,
                                                         @Param("path") String path, @Param("searchString") String searchString);

    @Query("select l from logs l where service_id in :ids and timestamp <= :timestampTo AND path LIKE %:path% AND log like %:searchString%")
    Page<CollectedLog> findByServiceIdsWithToTimestamp(Pageable pageable, @Param("ids") List<UUID> servicesIds,
                                                       @Param("timestampTo") Timestamp timestamp,
                                                       @Param("path") String path, @Param("searchString") String searchString);

    @Query("select l from logs l where service_id in :ids and timestamp >= :timestampFrom and timestamp <= :timestampTo AND path LIKE %:path% AND log like %:searchString%")
    Page<CollectedLog> findByServiceIdsWithTimestamp(Pageable pageable, @Param("ids") List<UUID> servicesIds,
                                                     @Param("timestampFrom") Timestamp timestampFrom,
                                                     @Param("timestampTo") Timestamp timestampTo,
                                                     @Param("path") String path, @Param("searchString") String searchString);
    @Query("SELECT MAX(id) from logs")
    long getLastId();

    // TODO: Add path here
    @Query("select l from logs l where service_id = :id AND log like %:searchString% AND path LIKE %:path% AND id >= :startId AND id <= :endId")
    ArrayList<CollectedLog> findByAlertConfiguration(@Param("id") UUID serviceId, @Param("searchString") String searchString, @Param("path") String pathSearchString,
            @Param("startId") Long startId, @Param("endId") Long endId);
}

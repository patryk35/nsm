package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pdm.networkservicesmonitor.model.agent.Packet;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface PacketRepository extends JpaRepository<Packet, UUID> {

    @Query(value = "SELECT * FROM received_packets WHERE agent_id = :agentId ORDER BY receiving_timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<Packet> findLastByAgentId(@Param("agentId") UUID agentId);
}

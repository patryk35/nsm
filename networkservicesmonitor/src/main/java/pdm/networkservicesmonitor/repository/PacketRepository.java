package pdm.networkservicesmonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pdm.networkservicesmonitor.model.agent.Packet;

import java.util.Optional;
import java.util.UUID;

public interface PacketRepository extends JpaRepository<Packet, UUID> {

    @Query(value = "SELECT * FROM received_packets WHERE agent_id = :agentId ORDER BY receiving_timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<Packet> findLastByAgentId(@Param("agentId") UUID agentId);
}

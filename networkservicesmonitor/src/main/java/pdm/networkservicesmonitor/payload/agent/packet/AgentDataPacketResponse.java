package pdm.networkservicesmonitor.payload.agent.packet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AgentDataPacketResponse {
    private UUID agentId;
    private UUID packetId;
}

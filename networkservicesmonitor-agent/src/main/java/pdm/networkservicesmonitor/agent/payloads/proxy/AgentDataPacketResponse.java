package pdm.networkservicesmonitor.agent.payloads.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentDataPacketResponse {
    private UUID agentId;
    private UUID packetId;
}

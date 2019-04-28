package pdm.networkservicesmonitor.payload.agent;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class AgentRequest {
    @Getter @Setter
    private UUID agentId;
}

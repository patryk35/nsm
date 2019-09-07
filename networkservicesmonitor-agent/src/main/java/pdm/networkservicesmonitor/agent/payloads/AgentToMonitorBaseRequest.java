package pdm.networkservicesmonitor.agent.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class AgentToMonitorBaseRequest {
    @Getter
    @Setter
    private UUID agentId;
}

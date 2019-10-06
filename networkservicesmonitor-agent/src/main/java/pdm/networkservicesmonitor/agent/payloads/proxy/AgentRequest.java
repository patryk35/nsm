package pdm.networkservicesmonitor.agent.payloads.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentRequest {
    @NotNull
    private UUID agentId;
}

package pdm.networkservicesmonitor.payload.agent.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentAuthCheckRequest {
    private String token;
    private String requestIp;
}

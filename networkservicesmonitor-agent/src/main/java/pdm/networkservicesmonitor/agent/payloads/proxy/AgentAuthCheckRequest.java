package pdm.networkservicesmonitor.agent.payloads.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentAuthCheckRequest {
    private String token;
    private String requestIp;
}

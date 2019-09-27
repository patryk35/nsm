package pdm.networkservicesmonitor.payload.client.agent;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AgentDetailsResponse {

    private UUID agentId;
    private String name;
    private String description;
    private String allowedOrigins;
    private boolean isRegistered;
    private Long sendingInterval;

}

package pdm.networkservicesmonitor.payload.client.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Data
public class AgentResponse {

    private UUID agentId;
    private String name;
    private String description;
    private String allowedOrigins;

    public AgentResponse(UUID id, String name, String description, String allowedOrigins) {
        this.agentId = id;
        this.name = name;
        this.description = description;
        this.allowedOrigins = allowedOrigins;

    }

}

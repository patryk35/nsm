package pdm.networkservicesmonitor.payload.client.agent;

import lombok.Data;

import java.util.UUID;

@Data
public class AgentResponse {

    private UUID agentId;
    private String name;
    private String description;
    private String allowedOrigins;
    private boolean isRegistered;

    public AgentResponse(UUID id, String name, String description, String allowedOrigins, boolean isRegistered) {
        this.agentId = id;
        this.name = name;
        this.description = description;
        this.allowedOrigins = allowedOrigins;
        this.isRegistered = isRegistered;
    }

}

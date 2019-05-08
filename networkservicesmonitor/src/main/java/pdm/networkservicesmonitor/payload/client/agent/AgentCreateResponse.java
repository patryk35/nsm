package pdm.networkservicesmonitor.payload.client.agent;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.client.ApiResponse;

import java.util.UUID;

@Slf4j
@Data
public class AgentCreateResponse extends ApiResponse {

    private UUID agentId;
    private UUID agentEncryptionKey;

    public AgentCreateResponse(Boolean success, String message, HttpStatus status, UUID agentId, UUID agentEncryptionKey) {
        super(success, message, status);
        this.agentId = agentId;
        this.agentEncryptionKey = agentEncryptionKey;

        log.debug(this.createLogMessage(
                String.format("agentID: %s,", agentId.toString()),
                String.format("agentEncryptionKey: %s,", agentEncryptionKey.toString())
        ));
    }
}

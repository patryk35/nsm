package pdm.networkservicesmonitor.payload.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.client.ApiResponse;

import java.util.UUID;

@Data
@Slf4j
public class AgentBaseResponse extends ApiResponse {

    private UUID agentId;

    public AgentBaseResponse(Boolean success, String message, HttpStatus status) {
        super(success, message, status);
        log.debug(createLogMessage());
    }
}

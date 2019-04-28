package pdm.networkservicesmonitor.payload.agent;

import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.client.ApiResponse;

public class AgentDataResponse extends ApiResponse {
    public AgentDataResponse(Boolean success, String message, HttpStatus status) {
        super(success, message, status);
    }
}

package pdm.networkservicesmonitor.payload.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.client.ApiResponse;

@Slf4j
@Data
public class AgentRegistrationResponse extends ApiResponse {

    private Boolean registrationStatus;

    public AgentRegistrationResponse(Boolean success, String message, HttpStatus status, Boolean registrationStatus) {
        super(success, message, status);
        this.registrationStatus = registrationStatus;
        log.debug(this.createLogMessage(String.format("registrationStatus: %s,", registrationStatus)));

    }
}

package pdm.networkservicesmonitor.agent.payloads.proxy;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

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

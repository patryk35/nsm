package pdm.networkservicesmonitor.payload.agent;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.client.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.ApiResponse;

@Slf4j
public class AgentRegistrationResponse extends ApiResponse {

    @Setter
    @Getter
    private Boolean registrationStatus;

    public AgentRegistrationResponse(Boolean success, String message, HttpStatus status, Boolean registrationStatus) {
        super(success, message, status);
        this.registrationStatus = registrationStatus;
        log.debug(this.createLogMessage(String.format("registrationStatus: %s,", registrationStatus)));

    }
}

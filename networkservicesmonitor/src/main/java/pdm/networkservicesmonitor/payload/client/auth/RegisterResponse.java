package pdm.networkservicesmonitor.payload.client.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.client.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.ApiResponse;

@Slf4j
public class RegisterResponse extends ApiResponse {

    @Getter
    @Setter
    private boolean isFirstAccount;

    public RegisterResponse(Boolean success, String message, HttpStatus status, Boolean isFirstAccount) {
        super(success, message, status);
        this.isFirstAccount=isFirstAccount;
        log.debug(this.createLogMessage(String.format("isFirstAccount: %b,", isFirstAccount)));

    }

}

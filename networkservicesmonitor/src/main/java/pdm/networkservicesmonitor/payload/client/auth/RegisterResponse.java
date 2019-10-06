package pdm.networkservicesmonitor.payload.client.auth;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.ApiResponse;

@Slf4j
@Data
public class RegisterResponse extends ApiResponse {

    private boolean isFirstAccount;

    public RegisterResponse(Boolean success, String message, HttpStatus status, Boolean isFirstAccount) {
        super(success, message, status);
        this.isFirstAccount = isFirstAccount;
        log.debug(createLogMessage(String.format("isFirstAccount: %b,", isFirstAccount)));

    }

}

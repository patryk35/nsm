package pdm.networkservicesmonitor.agent.payloads.proxy;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;


@Slf4j
@NoArgsConstructor
public class ApiBaseResponse extends ApiResponse {

    public ApiBaseResponse(Boolean success, String message, HttpStatus status) {
        super(success, message, status);
        log.debug(this.createLogMessage());
    }

}
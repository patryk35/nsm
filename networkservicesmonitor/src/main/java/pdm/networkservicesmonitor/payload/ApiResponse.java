package pdm.networkservicesmonitor.payload;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;


@Slf4j
public abstract class ApiResponse {
    @Getter
    @Setter
    protected Boolean success;
    @Getter @Setter
    protected String message;
    @Getter @Setter
    protected int status;
    @Getter @Setter
    protected String error;

    public ApiResponse(Boolean success, String message, HttpStatus status) {
        this.success = success;
        this.message = message;
        this.status = status.value();
        this.error = status.getReasonPhrase();

    }

}
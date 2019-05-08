package pdm.networkservicesmonitor.payload.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Slf4j
@Data
public class CreateResponse extends ApiResponse {
    private String id;

    public CreateResponse(Boolean success, String message, HttpStatus status, String id) {
        super(success, message, status);
        this.id = id;

        log.debug(this.createLogMessage(
                String.format("\tid: %s,", id)
        ));
    }

    public CreateResponse(Boolean success, String message, HttpStatus status, UUID id) {
        this(success,message,status,id.toString());
    }

    public CreateResponse(Boolean success, String message, HttpStatus status, Long id) {
        this(success,message,status,id.toString());
    }
}

package pdm.networkservicesmonitor.agent.connection;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class MonitorConnectionException extends RuntimeException {

    @Getter
    private HttpStatus status;

    @Getter
    private String monitorMessage;

    public MonitorConnectionException(String message, HttpStatus status, String monitorMessage) {
        super(message);
        this.status = status;
        this.monitorMessage= monitorMessage;
    }

    public MonitorConnectionException(String message) {
        super(message);
    }
}

package pdm.networkservicesmonitor.exceptions;

import org.springframework.http.HttpHeaders;

public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }
}
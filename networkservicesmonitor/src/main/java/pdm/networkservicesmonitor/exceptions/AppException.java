package pdm.networkservicesmonitor.exceptions;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
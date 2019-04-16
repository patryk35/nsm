package pdm.networkservicesmonitor.exceptions;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class AppException extends RuntimeException {

    @Getter
    protected Map<Object,Object> additionalEntries;

    public AppException(String message) {
        super(message);
        additionalEntries = new HashMap<>();
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
        additionalEntries = new HashMap<>();
    }
}
package pdm.networkservicesmonitor.exceptions;

public class MethodNotAllowed extends AppException {

    public MethodNotAllowed(String message) {
        super(message);
    }

    public MethodNotAllowed(String message, Throwable cause) {
        super(message, cause);
    }
}
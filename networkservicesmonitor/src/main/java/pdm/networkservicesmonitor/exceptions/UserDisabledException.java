package pdm.networkservicesmonitor.exceptions;

public class UserDisabledException extends AppException {
    public UserDisabledException(String message) {
        super(message);
    }

    public UserDisabledException(String message, Throwable cause) {
        super(message, cause);
    }
}
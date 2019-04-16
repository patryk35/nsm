package pdm.networkservicesmonitor.exceptions;

public class UserBadCredentialsException extends AppException {
    public UserBadCredentialsException(String message) {
        super(message);
    }

    public UserBadCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}

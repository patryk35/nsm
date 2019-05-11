package pdm.networkservicesmonitor.exceptions;

public class AppNotImplementedException extends AppException {

    public AppNotImplementedException(String message, String notes) {
        super(String.format("%s. Additional notes:  %s.", message, notes));
    }

}

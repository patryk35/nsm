package pdm.networkservicesmonitor.exceptions;

public class AppNotImplementedException extends AppException{

    public AppNotImplementedException(String message, String notes) {
        super(message);
        additionalEntries.put("notes",notes);
    }

    public AppNotImplementedException(String message, String notes, Throwable cause) {
        super(message, cause);
        additionalEntries.put("notes",notes);
    }
}

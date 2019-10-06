package pdm.networkservicesmonitor.payload.client.auth;

import lombok.Data;
import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.ApiResponse;

@Data

public class DataAvailability extends ApiResponse {
    private Boolean available;

    public DataAvailability(Boolean available, Boolean success, String message, HttpStatus status) {
        super(success, message, status);
        this.available = available;
        String.format("\tavailable: %b,", available);
    }
}
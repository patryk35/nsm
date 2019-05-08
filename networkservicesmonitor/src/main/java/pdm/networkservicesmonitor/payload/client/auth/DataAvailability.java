package pdm.networkservicesmonitor.payload.client.auth;

import lombok.Data;
import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.client.ApiBaseResponse;

@Data
public class DataAvailability extends ApiBaseResponse {
    private Boolean available;

    public DataAvailability(Boolean available, Boolean success, String message, HttpStatus status) {
        super(success, message, status);
        this.available=available;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ApiBaseResponse: {")
                .append("\tavailable: ").append(available).append(",")
                .append("\tsuccess: ").append(success).append(",")
                .append("\tmessage: ").append(message).append(",")
                .append("\tstatus: ").append(status).append(",")
                .append("\treason: ").append(reason).append(",")
                .append("\tadditionalEntries: {")
                .append("\t}")
                .append("}").toString();
    }
}
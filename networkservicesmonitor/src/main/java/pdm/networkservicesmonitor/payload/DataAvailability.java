package pdm.networkservicesmonitor.payload;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class DataAvailability extends ApiResponse{
    private Boolean available;

    public DataAvailability(Boolean available, Boolean success, String message, HttpStatus status) {
        super(success, message, status);
        this.available=available;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ApiResponse: {")
                .append("\tavailable: ").append(available).append(",")
                .append("\tsuccess: ").append(success).append(",")
                .append("\tmessage: ").append(message).append(",")
                .append("\tstatus: ").append(status).append(",")
                .append("\terror: ").append(error).append(",")
                .append("\tadditionalEntries: {")
                .append("\t}")
                .append("}").toString();
    }
}
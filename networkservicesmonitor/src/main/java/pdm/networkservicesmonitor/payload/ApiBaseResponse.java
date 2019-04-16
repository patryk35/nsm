package pdm.networkservicesmonitor.payload;

import org.springframework.http.HttpStatus;

public class ApiBaseResponse extends ApiResponse {

    public ApiBaseResponse(Boolean success, String message, HttpStatus status) {
        super(success, message, status);

    }

    @Override
    public String toString() {
        return new StringBuilder().append("ApiResponse: {")
                .append("\tsuccess: ").append(success).append(",")
                .append("\tmessage: ").append(message).append(",")
                .append("\tstatus: ").append(status).append(",")
                .append("\terror: ").append(error).append(",")
                .append("\tadditionalEntries: {")
                .append("\t}")
                .append("}").toString();
    }
}

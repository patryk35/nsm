package pdm.networkservicesmonitor.payload.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

public abstract class ApiResponse {
    @Getter
    @Setter
    protected Boolean success;
    @Getter
    @Setter
    protected String message;
    @Getter
    @Setter
    protected int status;
    @Getter
    @Setter
    protected String reason;

    public ApiResponse(Boolean success, String message, HttpStatus status) {
        this.success = success;
        this.message = message;
        this.status = status.value();
        this.reason = status.getReasonPhrase();
    }

    protected String createLogMessage(String... entries) {

        StringBuilder sb = new StringBuilder().append("ApiBaseResponse: {")
                .append("\tsuccess: ").append(success).append(",")
                .append("\tmessage: ").append(message).append(",")
                .append("\tstatus: ").append(status).append(",")
                .append("\treason: ").append(reason).append(",");

        Arrays.stream(entries).forEach(e -> sb.append(String.format("\t%s", e)));
        sb.append("}");
        return sb.toString();
    }
}

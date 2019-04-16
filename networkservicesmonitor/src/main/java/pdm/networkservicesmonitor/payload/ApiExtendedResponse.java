package pdm.networkservicesmonitor.payload;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Slf4j
public class ApiExtendedResponse extends ApiResponse {
    @Getter
    @Setter
    private Map<Object, Object> additionalEntries;
    public ApiExtendedResponse(Boolean success, String message, HttpStatus status, Map<Object, Object> additionalEntries) {
        super(success,message,status);
        this.additionalEntries = additionalEntries;
        log.debug(this.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("ApiResponse: {")
                .append("\tsuccess: ").append(success).append(",")
                .append("\tmessage: ").append(message).append(",")
                .append("\tstatus: ").append(status).append(",")
                .append("\terror: ").append(error).append(",")
                .append("\tadditionalEntries: {");
        if(!additionalEntries.equals(null))
            additionalEntries.forEach((k,v) -> sb.append("\t\t").append(k).append(": ").append(v).append(","));
        sb.append("\t}").append("}");
        return sb.toString();
    }
}

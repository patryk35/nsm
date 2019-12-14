package pdm.networkservicesmonitor.payload;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
public class ApiQueryResponse extends ApiBaseResponse {

    private String queryError;
    public ApiQueryResponse(Boolean success, String message, HttpStatus status, String queryError) {
        super(success, message, status);
        this.queryError = queryError;
    }
}

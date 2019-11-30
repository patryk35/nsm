package pdm.networkservicesmonitor.payload;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
public class QueryApiResponse extends ApiBaseResponse {

    private String queryError;
    public QueryApiResponse(Boolean success, String message, HttpStatus status, String queryError) {
        super(success, message, status);
        this.queryError = queryError;
    }
}

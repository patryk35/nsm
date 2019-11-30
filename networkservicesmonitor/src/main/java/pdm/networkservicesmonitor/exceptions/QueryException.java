package pdm.networkservicesmonitor.exceptions;

import lombok.Data;

@Data
public class QueryException extends AppException {

    private String queryError;
    public QueryException(String resourceName, String fieldName, Object fieldValue, String queryError) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.queryError = queryError;
    }
}
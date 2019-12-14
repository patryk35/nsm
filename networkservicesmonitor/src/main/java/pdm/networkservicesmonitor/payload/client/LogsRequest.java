package pdm.networkservicesmonitor.payload.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.config.AppConstants;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogsRequest {

    @NotBlank
    private String query;

    private int page = AppConstants.DEFAULT_LOGS_PAGE_NUMBER;
    private int size = AppConstants.DEFAULT_LOGS_PAGE_SIZE;
    private String datetimeFrom;
    private String datetimeTo;
}

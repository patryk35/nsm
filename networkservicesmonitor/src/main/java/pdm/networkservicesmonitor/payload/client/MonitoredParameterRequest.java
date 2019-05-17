package pdm.networkservicesmonitor.payload.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitoredParameterRequest {
    @NotBlank
    private String query;

    private String datetimeFrom;
    private String datetimeTo;
}

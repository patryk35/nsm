package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LogsAlertCreateRequest {
    @NotNull
    private UUID serviceId;
    @NotNull
    private String message;
    @NotNull
    private String pathSearchSting;
    @NotNull
    private String searchString;
}

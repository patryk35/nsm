package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogsAlertEditRequest {
    @NotNull
    private UUID alertId;
    @NotNull
    private String message;
    @NotNull
    private String pathSearchSting;
    @NotNull
    private String searchString;
    @NotNull
    private boolean enabled;
}

package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MonitoringAlertEditRequest {
    @NotNull
    private UUID alertId;
    @NotNull
    @Size(min = 3, max = 200)
    private String message;
    @NotNull
    @Size(min = 1, max = 5)
    private String condition;
    @NotNull
    @Size(min = 1, max = 200)
    private String value;
    @NotNull
    private boolean enabled;
}

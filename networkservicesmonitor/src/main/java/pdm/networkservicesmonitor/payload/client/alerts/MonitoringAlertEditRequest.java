package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MonitoringAlertEditRequest {
    @NotNull
    private UUID alertId;
    @NotNull
    private String message;
    @NotNull
    private String condition;
    @NotNull
    private String value;
    @NotNull
    private boolean enabled;
}

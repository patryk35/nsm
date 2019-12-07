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
public class MonitoringAlertConfigurationEditRequest {
    @NotNull
    private UUID alertId;
    @NotNull
    @Size(min = 3, max = 200)
    private String message;
    @NotNull
    @Size(min = 1, max = 5)
    private String condition;
    @NotNull
    private double value;
    @NotNull
    private boolean enabled;
    @NotNull
    private String alertLevel;
    @NotNull
    private boolean emailNotification;
    @NotNull
    private String recipients;
}

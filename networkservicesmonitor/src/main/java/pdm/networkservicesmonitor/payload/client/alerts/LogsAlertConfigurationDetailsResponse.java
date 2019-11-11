package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.alert.AlertLevel;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LogsAlertConfigurationDetailsResponse {
    @NotNull
    private UUID id;
    @NotNull
    private UUID serviceId;
    @NotNull
    private String serviceName;
    @NotNull
    private UUID agentId;
    @NotNull
    private String agentName;
    @NotNull
    private String message;
    @NotNull
    private String pathSearchString;
    @NotNull
    private String searchString;
    @NotNull
    private boolean enabled;
    @NotNull
    private boolean deleted;
    @NotNull
    private AlertLevel alertLevel;
}

package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.config.AlertLevel;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MonitoringAlertConfigurationDetailsResponse {
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
    private UUID monitoredParameterType;
    @NotNull
    private String monitoredParameterTypeName;
    @NotNull
    private String message;
    @NotNull
    private String condition;
    @NotNull
    private String value;
    @NotNull
    private boolean enabled;
    @NotNull
    private boolean deleted;
    @NotNull
    private AlertLevel alertLevel;
}

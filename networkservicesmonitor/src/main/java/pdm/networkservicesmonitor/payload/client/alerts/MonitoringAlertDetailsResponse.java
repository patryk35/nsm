package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import pdm.networkservicesmonitor.model.alert.AlertLevel;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class MonitoringAlertDetailsResponse {
    @NotNull
    private Long id;
    @NotNull
    private String agentName;
    @NotNull
    private String serviceName;
    @NotNull
    private String parameterTypeName;
    @NotNull
    private Timestamp timestamp;
    @NotNull
    private String message;
    @NotNull
    private String condition;
    @NotNull
    private double limitValue;
    @NotNull
    private double measuredValue;
    @NotNull
    private AlertLevel alertLevel;
}

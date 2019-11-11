package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.alert.AlertLevel;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogsAlertResponse {
    @NotNull
    private Long id;
    @NotNull
    private String agentName;
    @NotNull
    private String serviceName;
    @NotNull
    private Timestamp timestamp;
    @NotNull
    private String message;
    @NotNull
    private AlertLevel alertLevel;
}

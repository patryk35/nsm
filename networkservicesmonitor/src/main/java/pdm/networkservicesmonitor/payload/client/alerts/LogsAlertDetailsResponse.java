package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import pdm.networkservicesmonitor.model.alert.AlertLevel;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class LogsAlertDetailsResponse {
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
    private String pathSearchString;
    @NotNull
    private String searchString;
    @NotNull
    private String log;
    @NotNull
    private AlertLevel alertLevel;
}

package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogsAlertEditRequest {
    @NotNull
    private UUID alertId;
    @NotNull
    @Size(min = 3, max = 200)
    private String message;
    @NotNull
    @Size(max = 200)
    private String pathSearchString;
    @NotNull
    @Size(max = 200)
    private String searchString;
    @NotNull
    private boolean enabled;
}

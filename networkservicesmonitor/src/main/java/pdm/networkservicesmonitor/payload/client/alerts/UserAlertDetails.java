package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import pdm.networkservicesmonitor.model.alert.AlertLevel;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class UserAlertDetails {
    @NotNull
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private String email;

    @NotNull
    private String fullname;

    @NotNull
    private String username;

    @NotNull
    private String message;

    @NotNull
    private Timestamp timestamp;

    @NotNull
    private AlertLevel alertLevel;
}

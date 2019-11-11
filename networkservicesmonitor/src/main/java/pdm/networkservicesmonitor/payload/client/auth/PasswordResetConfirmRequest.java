package pdm.networkservicesmonitor.payload.client.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetConfirmRequest {
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;
    @NotNull
    private UUID resetKey;
}

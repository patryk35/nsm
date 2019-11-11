package pdm.networkservicesmonitor.payload.client.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AuthenticationRequest {
    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;

    @NotNull
    private Boolean rememberMe;

}
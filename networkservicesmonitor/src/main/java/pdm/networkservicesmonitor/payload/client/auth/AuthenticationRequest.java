package pdm.networkservicesmonitor.payload.client.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AuthenticationRequest {
    @NotBlank
    @Getter @Setter
    private String usernameOrEmail;

    @NotBlank
    @Getter @Setter
    private String password;

    @NotNull
    @Getter @Setter
    private Boolean rememberMe;

}
package pdm.networkservicesmonitor.payload.client.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthenticationRequest {
    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;
}
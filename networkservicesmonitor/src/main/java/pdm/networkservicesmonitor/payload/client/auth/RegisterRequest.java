package pdm.networkservicesmonitor.payload.client.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 60)
    @Getter
    @Setter
    private String fullname;


    @NotBlank
    @Size(min = 3, max = 60)
    @Getter
    @Setter
    private String username;

    @NotBlank
    @Size(max = 60)
    @Email
    @Getter
    @Setter
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Getter
    @Setter
    private String password;

}
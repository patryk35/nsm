package pdm.networkservicesmonitor.payload;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 60)
    @Getter @Setter
    private String fullname;


    @NotBlank
    @Size(min = 3, max = 60)
    @Getter @Setter
    private String username;

    @NotBlank
    @Size(max = 60)
    @Email
    @Getter @Setter
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Getter @Setter
    private String password;

}
package pdm.networkservicesmonitor.payload.client.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Data
@AllArgsConstructor
public class UserDetails {
    @NotNull
    private Long id;
    @NotNull
    private String username;
    @NotNull
    private String name;
    @NotNull
    private Collection<? extends GrantedAuthority> authorities;
}

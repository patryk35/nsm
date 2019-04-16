package pdm.networkservicesmonitor.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
@AllArgsConstructor
public class UserDetails {
    private Long id;
    private String username;
    private String name;
    private Collection<? extends GrantedAuthority> authorities;
}

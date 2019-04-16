package pdm.networkservicesmonitor.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pdm.networkservicesmonitor.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserSecurityDetails implements UserDetails {
    private Long id;
    private String fullname;
    private String username;
    @JsonIgnore
    private String email;
    @JsonIgnore
    private String password;
    private Boolean isEnabled;
    private Collection<? extends GrantedAuthority> authorities;


    public static UserSecurityDetails create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().name())
        ).collect(Collectors.toList());

        return new UserSecurityDetails(
                user.getId(),
                user.getFullname(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getIsEnabled(),
                authorities
        );
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    // TODO(MEDIUM): Use below propertie in admin panel, e.g. for disabling user
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSecurityDetails that = (UserSecurityDetails) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
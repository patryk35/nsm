package pdm.networkservicesmonitor.model.user;

import lombok.*;
import pdm.networkservicesmonitor.model.audit.TimeAudit;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username"
        }),
        @UniqueConstraint(columnNames = {
                "email"
        })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends TimeAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 60)
    private String fullname;

    @NotBlank
    @Size(min = 3, max = 40)
    @Setter(AccessLevel.NONE)
    private String username;

    @NotBlank
    @Size(max = 60)
    @Email
    private String email;

    @NotBlank
    @Size(min = 60, max = 60)
    private String password;

    @NotNull
    private boolean enabled;

    @NotNull
    private boolean emailVerified;

    @NotNull
    private boolean activated;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Setter(AccessLevel.NONE)
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<MailKey> mailKeys;


    public User(String fullname, String username, String email, String password) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = false;
        this.emailVerified = false;
        this.activated = false;
        this.roles = new HashSet<>();

    }

    public void addRole(Role userRole) {
        roles.add(userRole);
    }

    public void removeRole(Role userRole){
        roles.remove(userRole);
    }
}
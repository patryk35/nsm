package pdm.networkservicesmonitor.model;

import lombok.*;
import org.hibernate.annotations.NaturalId;
import pdm.networkservicesmonitor.model.audit.TimeAudit;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
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
// TODO(HIGH): Extend with avatar color
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

    @NaturalId
    @NotBlank
    @Size(max = 60)
    @Email
    private String email;

    @NotBlank
    @Size(min=60, max=60)
    private String password;

    @NotNull
    private Boolean isEnabled;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();


    public User(String fullname, String username, String email, String password, Boolean isEnabled) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isEnabled = isEnabled;
    }
}
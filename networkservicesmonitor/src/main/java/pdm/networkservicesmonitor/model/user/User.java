package pdm.networkservicesmonitor.model.user;

import lombok.*;
import pdm.networkservicesmonitor.exceptions.BadRequestException;
import pdm.networkservicesmonitor.model.audit.TimeAudit;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    @Column(columnDefinition = "TEXT")
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> accessTokens = new ArrayList<>();

    public User(String fullname, String username, String email, String password) {
        if (!validatePassword(password)) {
            throw new BadRequestException("Password has to have at leas 1 small and 1 big letter and at least 1 number and special char");
        }
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

    public void removeRole(Role userRole) {
        roles.remove(userRole);
    }

    public void setUserPassword(String password) {
        if (!validatePassword(password)) {
            throw new BadRequestException("Password has to have at leas 1 small and 1 big letter and at least 1 number and special char");
        }
        this.password = password;
    }

    private boolean validatePassword(String password) {

        Pattern sLetter = Pattern.compile("[a-z]");
        Pattern bLetter = Pattern.compile("[A-z]");

        Pattern digit = Pattern.compile("[0-9]");
        Pattern special = Pattern.compile("[!@#$%^&*()_+-=/[/]{};':\"/|,.<>?\\/]");


        Matcher hasSLetter = sLetter.matcher(password);
        Matcher hasBLetter = bLetter.matcher(password);
        Matcher hasDigit = digit.matcher(password);
        Matcher hasSpecial = special.matcher(password);

        return hasSLetter.find() && hasBLetter.find() && hasDigit.find() && hasSpecial.find();

    }
}
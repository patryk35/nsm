package pdm.networkservicesmonitor.model.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.alert.AlertLevel;
import pdm.networkservicesmonitor.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity(name = "user_alerts")
public class UserAlert {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private User user;

    @NotNull
    private String message;

    @NotNull
    private Timestamp timestamp;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;


    public UserAlert(@NotNull User user, @NotNull String message, @NotNull Timestamp timestamp, @NotNull AlertLevel alertLevel) {
        this.user = user;
        this.message = message;
        this.timestamp = timestamp;
        this.alertLevel = alertLevel;
    }
}

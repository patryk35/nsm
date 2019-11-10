package pdm.networkservicesmonitor.model.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.config.AlertLevel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity
public class UserAlert {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private String message;

    @NotNull
    private Timestamp timestamp;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;


    public UserAlert(@NotNull Long userId, @NotNull String message, @NotNull Timestamp timestamp, @NotNull AlertLevel alertLevel) {
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
        this.alertLevel = alertLevel;
    }
}

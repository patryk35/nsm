package pdm.networkservicesmonitor.model.alert;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pdm.networkservicesmonitor.model.audit.TimeAndUserAudit;
import pdm.networkservicesmonitor.model.service.Service;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Entity(name = "logs_alerts_configurations")
@Data
@NoArgsConstructor
public class LogsAlertConfiguration extends TimeAndUserAudit {
    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Service service;
    @NotNull
    @Size(min = 1, max = 200)
    private String message;
    @NotNull
    @Size(max = 200)
    private String pathSearchString;
    @NotNull
    @Size(max = 200)
    private String searchString;
    @NotNull
    private boolean enabled = true;
    @NotNull
    private boolean deleted = false;
    @NotNull
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;

    @NotNull
    private boolean emailNotification;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> recipients;

    public LogsAlertConfiguration(Service service, @NotNull String message, @NotNull String pathSearchString, @NotNull String searchString, @NotNull AlertLevel alertLevel,
                                  @NotNull boolean emailNotification, @NotNull List<String> recipients) {
        this.service = service;
        this.message = message;
        this.pathSearchString = pathSearchString;
        this.searchString = searchString;
        this.alertLevel = alertLevel;
        this.emailNotification = emailNotification;
        this.recipients = recipients;
    }
}

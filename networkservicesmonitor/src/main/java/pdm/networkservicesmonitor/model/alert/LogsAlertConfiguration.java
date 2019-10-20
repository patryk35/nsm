package pdm.networkservicesmonitor.model.alert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.NaturalId;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.model.audit.TimeAndUserAudit;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.model.data.LogsAlert;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity(name = "logs_alerts_configuration")
@Data
@NoArgsConstructor
public class LogsAlertConfiguration extends TimeAndUserAudit {
    @GeneratedValue
    @Id
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

    public LogsAlertConfiguration(Service service, @NotNull String message, @NotNull String pathSearchString, @NotNull String searchString) {
        this.service = service;
        this.message = message;
        this.pathSearchString = pathSearchString;
        this.searchString = searchString;
    }
}

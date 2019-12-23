package pdm.networkservicesmonitor.model.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.alert.LogsAlertConfiguration;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name = "logs_alerts")
@Data
@NoArgsConstructor
public class LogsAlert {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private LogsAlertConfiguration configuration;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private CollectedLog log;

    public LogsAlert(LogsAlertConfiguration configuration, @NotNull CollectedLog log) {
        this.configuration = configuration;
        this.log = log;
    }
}

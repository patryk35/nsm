package pdm.networkservicesmonitor.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pdm.networkservicesmonitor.model.alert.LogsAlertConfiguration;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
public class LogsAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

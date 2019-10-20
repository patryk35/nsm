package pdm.networkservicesmonitor.model.data;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.alert.MonitoringAlertConfiguration;
import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
public class MonitoringAlert {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private MonitoringAlertConfiguration configuration;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private MonitoredParameterValue value;

    public MonitoringAlert(MonitoringAlertConfiguration configuration, MonitoredParameterValue value) {
        this.configuration = configuration;
        this.value = value;
    }
}

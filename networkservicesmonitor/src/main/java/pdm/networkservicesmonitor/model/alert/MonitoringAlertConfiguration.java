package pdm.networkservicesmonitor.model.alert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.model.audit.TimeAndUserAudit;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity(name = "monitoring_alerts_configuration")
@Data
@NoArgsConstructor
public class MonitoringAlertConfiguration extends TimeAndUserAudit {
    @GeneratedValue
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Service service;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private MonitoredParameterType monitoredParameterType;
    @NotNull
    private String message;
    @NotNull
    private String condition;
    @NotNull
    private String value;
    @NotNull
    private boolean enabled = true;
    @NotNull
    private boolean deleted = false;

    public MonitoringAlertConfiguration(Service service, MonitoredParameterType monitoredParameterType, @NotNull String message, @NotNull String condition, @NotNull String value) {
        this.service = service;
        this.monitoredParameterType = monitoredParameterType;
        this.message = message;
        this.condition = condition;
        this.value = value;
    }
}

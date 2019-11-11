package pdm.networkservicesmonitor.model.alert;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.agent.service.Service;
import pdm.networkservicesmonitor.model.audit.TimeAndUserAudit;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity(name = "monitoring_alerts_configuration")
@Data
@NoArgsConstructor
public class MonitoringAlertConfiguration extends TimeAndUserAudit {
    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Service service;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private MonitoredParameterType monitoredParameterType;
    @NotNull
    @Size(min = 3, max = 200)
    private String message;
    @NotNull
    @Size(min = 1, max = 5)
    private String condition;
    @NotNull
    @Size(min = 1, max = 200)
    private String value;
    @NotNull
    private boolean enabled = true;
    @NotNull
    private boolean deleted = false;
    @NotNull
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;

    public MonitoringAlertConfiguration(Service service, MonitoredParameterType monitoredParameterType, @NotNull String message, @NotNull String condition, @NotNull String value, @NotNull AlertLevel alertLevel) {
        this.service = service;
        this.monitoredParameterType = monitoredParameterType;
        this.message = message;
        this.condition = condition;
        this.value = value;
        this.alertLevel = alertLevel;
    }
}
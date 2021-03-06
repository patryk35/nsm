package pdm.networkservicesmonitor.model.alert;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pdm.networkservicesmonitor.model.audit.TimeAndUserAudit;
import pdm.networkservicesmonitor.model.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.service.Service;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Entity(name = "monitoring_alerts_configurations")
@Data
@NoArgsConstructor
public class MonitoringAlertConfiguration extends TimeAndUserAudit {
    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private Service service;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private MonitoredParameterType monitoredParameterType;
    @NotNull
    @Size(min = 3, max = 200)
    private String message;
    @NotNull
    @Size(min = 1, max = 5)
    private String condition;
    @NotNull
    private double value;
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

    public MonitoringAlertConfiguration(Service service, MonitoredParameterType monitoredParameterType, @NotNull String message, @NotNull String condition, @NotNull double value, @NotNull AlertLevel alertLevel,
                                        @NotNull boolean emailNotification, @NotNull List<String> recipients) {
        this.service = service;
        this.monitoredParameterType = monitoredParameterType;
        this.message = message;
        this.condition = condition;
        this.value = value;
        this.alertLevel = alertLevel;
        this.emailNotification = emailNotification;
        this.recipients = recipients;
    }
}

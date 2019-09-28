package pdm.networkservicesmonitor.model.agent.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity(name = "monitored_parameters_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredParameterConfiguration {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;


    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_type_id")
    private MonitoredParameterType parameterType;

    @Transient
    private UUID parameterId;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotNull
    private boolean isDeleted = false;

    @NotNull
    private Long monitoringInterval = 1000L;

    public MonitoredParameterConfiguration(MonitoredParameterType parameterType, Service service, String description, Long monitoringInterval) {
        this.parameterType = parameterType;
        this.parameterId = parameterType.getId();
        this.service = service;
        this.description = description;
        this.monitoringInterval = monitoringInterval;
    }

}

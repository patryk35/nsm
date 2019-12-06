package pdm.networkservicesmonitor.model.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "monitored_parameter_types")
public class MonitoredParameterType {
    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;

    private UUID parentId;
    @NotBlank
    @Size(max = 512)
    private String name;
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;
    @NotBlank
    private String type;
    @NotNull
    private boolean systemParameter;

    @NotNull
    private boolean requireTargetObject;
    @NotNull
    private String targetObjectName;

    @NotNull
    private String unit;

    @NotNull
    private double multiplier;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parameterType")
    private List<MonitoredParameterConfiguration> monitoredParameterConfiguration;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parameterType")
    private List<MonitoredParameterValue> monitoredParameterValue;

    public MonitoredParameterType(UUID parentId, @NotBlank @Size(max = 512) String name, @NotBlank String description, @NotBlank String type, @NotNull boolean systemParameter, @NotNull boolean requireTargetObject, @NotNull String targetObjectName) {
        this.parentId = parentId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.systemParameter = systemParameter;
        this.requireTargetObject = requireTargetObject;
        this.targetObjectName = targetObjectName;
    }
}

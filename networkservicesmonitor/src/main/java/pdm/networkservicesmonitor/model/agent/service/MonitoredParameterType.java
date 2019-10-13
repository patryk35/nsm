package pdm.networkservicesmonitor.model.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
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
    @NotBlank
    @Size(max = 512)
    private String name;
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;
    @NotBlank
    private String type;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parameterType")
    private List<MonitoredParameterConfiguration> monitoredParameterConfiguration;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parameterType")
    private List<MonitoredParameterValue> monitoredParameterValue;
}

package pdm.networkservicesmonitor.model.agent.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity(name="monitored_parameters_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredParameterConfiguration {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;

    @NotBlank
    @Size(max = 200)
    private String description;

    private Long latency;

}

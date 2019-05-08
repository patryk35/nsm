package pdm.networkservicesmonitor.agent.agent_configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredParameter {

    private UUID id;

    @NotBlank
    @Size(max = 200)
    private String description;

    private Long latency;

}

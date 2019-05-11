package pdm.networkservicesmonitor.agent.payloads.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredParameterConfiguration {

    @NotNull
    private UUID parameterId;

    private UUID serviceId;

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotNull
    private Long monitoringInterval;

    public MonitoredParameterConfiguration(@NotNull UUID parameterId, @NotBlank @Size(max = 200) String description, @NotNull Long monitoringInterval) {
        this.parameterId = parameterId;
        this.description = description;
        this.monitoringInterval = monitoringInterval;
    }
}

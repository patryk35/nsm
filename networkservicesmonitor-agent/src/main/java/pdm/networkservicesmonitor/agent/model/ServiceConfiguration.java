package pdm.networkservicesmonitor.agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceConfiguration {
    @NotNull
    private UUID id;

    @NotNull
    private List<LogsCollectingConfiguration> logsCollectingConfigurations;

    @NotNull
    private List<MonitoredParameterConfiguration> monitoredParametersConfigurations;

}
package pdm.networkservicesmonitor.payload.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import pdm.networkservicesmonitor.model.agent.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterConfiguration;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceConfiguration {
    @NotNull
    private UUID id;

    @NotNull
    private List<LogsCollectingConfiguration> logsCollectingConfigurations;

    @NotNull
    private List<MonitoredParameterConfiguration> monitoredParametersConfigurations;

}

package pdm.networkservicesmonitor.payload.agent.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import pdm.networkservicesmonitor.model.service.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.model.service.MonitoredParameterConfiguration;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceConfiguration {
    @NotNull
    private UUID serviceId;

    @NotNull
    private List<LogsCollectingConfiguration> logsCollectingConfigurations;

    @NotNull
    private List<MonitoredParameterConfiguration> monitoredParametersConfigurations;

}

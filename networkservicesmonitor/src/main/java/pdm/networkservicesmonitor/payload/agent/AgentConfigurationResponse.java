package pdm.networkservicesmonitor.payload.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.agent.configuration.ServiceLogsConfiguration;
import pdm.networkservicesmonitor.model.agent.configuration.MonitoredParameterConfiguration;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentConfigurationResponse {

    @NotNull
    private Long id;

    @NotNull
    private Long latency;

    @NotNull
    private List<ServiceLogsConfiguration> serviceLogsConfigurations;

    @NotNull
    private List<MonitoredParameterConfiguration> monitoredParametersConfigurations;

}

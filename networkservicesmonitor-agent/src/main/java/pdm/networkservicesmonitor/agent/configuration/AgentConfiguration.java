package pdm.networkservicesmonitor.agent.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.agent.payloads.configuration.ServiceConfiguration;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentConfiguration {
    @NotNull
    private UUID agentId;

    @NotNull
    private Long sendingInterval;

    @NotNull
    private List<ServiceConfiguration> servicesConfigurations;
}

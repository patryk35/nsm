package pdm.networkservicesmonitor.agent.payloads.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigurationResponse {

    @NotNull
    private UUID agentId;

    @NotNull
    private Long sendingInterval;

    @NotNull
    private List<ServiceConfiguration> servicesConfigurations;

    @NotNull
    private boolean isProxyAgent;
}

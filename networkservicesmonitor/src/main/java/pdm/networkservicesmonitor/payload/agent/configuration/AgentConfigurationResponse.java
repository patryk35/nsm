package pdm.networkservicesmonitor.payload.agent.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AgentConfigurationResponse {

    @NotNull
    private UUID agentId;

    @NotNull
    private Long sendingInterval;

    @NotNull
    private List<ServiceConfiguration> servicesConfigurations;


}

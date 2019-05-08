package pdm.networkservicesmonitor.agent.agent_configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.model.AgentConfiguration;

@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigurationManager {

    @Setter
    @Getter
    private AgentConfiguration agentConfiguration;



}

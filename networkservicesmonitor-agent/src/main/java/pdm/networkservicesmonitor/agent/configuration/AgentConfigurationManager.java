package pdm.networkservicesmonitor.agent.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.connection.MonitorWebClient;

@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigurationManager {

    @Autowired
    private MonitorWebClient monitorWebClient;

    private AgentConfiguration agentConfiguration;

    @Getter
    private boolean updated;

    public void downloadAgentConfiguration() {
        AgentConfiguration agentConfiguration = null;
        try {
            agentConfiguration = monitorWebClient.downloadAgentConfiguration();
        } catch (Exception e) {
            log.error(String.format("Agent configuration cannot be downloaded due to connection problems: %s", e.getMessage()));
            throw e;
        }
        if (agentConfiguration != null) {
            this.agentConfiguration = agentConfiguration;
        }
    }

    public void updateConfiguration() {
        log.info("Downloading new configuration!");
        downloadAgentConfiguration();
        updated = true;
    }

    public AgentConfiguration getAgentConfiguration() {
        log.info("Set new configuration!");
        log.info(agentConfiguration.toString());
        updated = false;
        return agentConfiguration;
    }

    public boolean isProxy(){
        return agentConfiguration.isProxyAgent();
    }

    public long getSendingInterval() {
        return agentConfiguration.getSendingInterval();
    }
}

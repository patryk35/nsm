package pdm.networkservicesmonitor.agent.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pdm.networkservicesmonitor.agent.configuration.AgentConfiguration;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.payloads.MonitorToAgentBaseResponse;
import pdm.networkservicesmonitor.agent.payloads.RegistrationStatusResponseToAgent;

import javax.servlet.ServletException;

@Service
@Slf4j
public class ConnectionManager {

    @Autowired
    public MonitorWebClient monitorWebClient;

    @Autowired
    public AgentConfigurationManager agentConfigurationManager;

    public void establishConnection() throws ServletException {
        try {
            RegistrationStatusResponseToAgent registrationStatusResponse = monitorWebClient.getRegistrationStatus();
            boolean isRegistered = registrationStatusResponse.getRegistrationStatus();
            log.info(String.format("Agent Registration Status: %b", isRegistered));
            if (!isRegistered) {
                MonitorToAgentBaseResponse response = monitorWebClient.registerAgent();
                if (!response.getSuccess()) {
                    throw new MonitorConnectionException("Problems during agent registered. Exiting.");
                } else {
                    log.info("Agent registered successfully!");
                }
            }
            downloadAgentConfiguration();
        } catch (WebClientResponseException exception) {
            log.error("Establishing connection problems");
            log.error(String.format("Monitor response %s: %s", exception.getRawStatusCode(), exception.getResponseBodyAsString()));
            throw new ServletException(exception);

        } catch (Exception e) {
            /* TODO(low) [for now it can work only with downloaded config]: It should load agent_configuration from some tmp file(or  SQLLite db), if monitor was working before.
                In case of existing tmp file and agent not registered - it should crash after receiving connection
                It should be some flag isFullyInitialized, if not - it should it background try to connect and check registration( and maybe more statuses) status
             */

            log.error("Agent cannot start");
            log.error("Cannot connect to monitor. Check monitor address, ip and uri configuration.");
            log.error(e.getMessage());
            throw new ServletException(e);
        }
    }

    private boolean downloadAgentConfiguration() {
        AgentConfiguration s = monitorWebClient.downloadAgentConfiguration();
        agentConfigurationManager.setAgentConfiguration(s);
        return true;
    }

}

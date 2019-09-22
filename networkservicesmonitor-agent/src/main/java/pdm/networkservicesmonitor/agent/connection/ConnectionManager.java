package pdm.networkservicesmonitor.agent.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.payloads.MonitorToAgentBaseResponse;
import pdm.networkservicesmonitor.agent.payloads.RegistrationStatusResponseToAgent;

import javax.servlet.ServletException;

@Service
@Slf4j
public class ConnectionManager {

    @Autowired
    private MonitorWebClient monitorWebClient;

    @Autowired
    private AgentConfigurationManager agentConfigurationManager;

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
            agentConfigurationManager.updateConfiguration();

            if (!agentConfigurationManager.isUpdated()) {
                throw new ServletException("Cannot load agent configuration ... Exiting ...");
            }
        } catch (WebClientResponseException exception) {
            log.error("Establishing connection problems");
            log.error(String.format("Monitor response %s: %s", exception.getRawStatusCode(), exception.getResponseBodyAsString()));
            throw new ServletException(exception);

        } catch (Exception e) {
            log.error("Agent cannot start");
            log.error("Cannot connect to monitor. Check monitor address, ip and uri configuration.");
            log.error(e.getMessage());
            throw new ServletException(e);
        }
    }

}

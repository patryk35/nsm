package pdm.networkservicesmonitor.agent.connection;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pdm.networkservicesmonitor.agent.model.Settings;
import pdm.networkservicesmonitor.agent.payloads.MonitorToAgentBaseResponse;
import pdm.networkservicesmonitor.agent.payloads.RegistrationStatusResponseToAgent;
import pdm.networkservicesmonitor.agent.settings.SettingsManager;

import javax.servlet.ServletException;

@Service
@Slf4j
public class ConnectionController {

    @Autowired
    public MonitorWebClient monitorWebClient;

    @Autowired
    public SettingsManager settingsManager;

    @Getter @Setter
    private boolean connectionStatus;

    // thread to check if connection is OK

    public void establishConnection() throws ServletException {
        try {
            RegistrationStatusResponseToAgent registrationStatusResponse = monitorWebClient.getRegistrationStatus();
            boolean isRegistered = registrationStatusResponse.getRegistrationStatus();
            log.info(String.format("Agent Registration Status: %b", isRegistered));
            if(!isRegistered) {
                MonitorToAgentBaseResponse response =  monitorWebClient.registerAgent();
                if(!response.getSuccess()){
                    throw new MonitorConnectionException("Monitor not registered. Exiting.");
                } else {
                    log.info("Agent registered successfully!");
                }
            }
            downloadAgentConfiguration();
        } catch (WebClientResponseException exception){
            log.error("Establishing connection problems");
            log.error(String.format("Monitor response %s: %s",exception.getRawStatusCode(), exception.getResponseBodyAsString()));
            throw new ServletException(exception);

        } catch (Exception e){
            /* TODO: It should load settings from some tmp file, if monitor was working before.
                In case of existing tmp file and agent not registered - it should crash after receiving connection
                It should be some flag isFullyInitialized, if not - it should it background try to connect and check registration( and maybe more statuses) status
             */

            log.error("Agent cannot start");
            log.error("Cannot connect to monitor. Check monitor address, ip and uri configuration.");
            log.error(e.getMessage());
            throw new ServletException(e);
        }
        connectionStatus = true;
    }

    public boolean sendDataToMonitor(){
        return false;
    }

    public boolean downloadAgentConfiguration(){
        //try {
            Settings s = monitorWebClient.downloadSettings();
            settingsManager.setSettings(s);
            return true;
       /* } catch (WebClientResponseException exception) {
            //TODO
            log.error("aaaaa");
            return false;
        }*/
    }

}

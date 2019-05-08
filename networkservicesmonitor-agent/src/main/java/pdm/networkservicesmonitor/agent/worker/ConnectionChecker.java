package pdm.networkservicesmonitor.agent.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.connection.ConnectionController;
import pdm.networkservicesmonitor.agent.connection.MonitorWebClient;

@Component
@Scope("prototype")
@Slf4j
public class ConnectionChecker implements Runnable{

    @Autowired
    private MonitorWebClient monitorWebClient;

    @Autowired
    private ConnectionController connectionController;

    @Override
    public void run() {
        while(true){

            try {
                //monitorWebClient.getRegistrationStatus();
                if(!connectionController.isConnectionStatus()){
                    connectionController.setConnectionStatus(true);
                    log.info("Connection to monitor is working again.");
                }
            } catch (Exception e){
                if(connectionController.isConnectionStatus()){
                    connectionController.setConnectionStatus(false);
                }
                log.error("Connection to monitor is broken.");
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

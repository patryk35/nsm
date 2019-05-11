package pdm.networkservicesmonitor.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.connection.ConnectionManager;
import pdm.networkservicesmonitor.agent.payloads.configuration.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ThreadsManager;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;

// TODO: shared classes between monitor and agent - https://stackoverflow.com/questions/23704903/how-to-share-code-between-two-projects
@SpringBootApplication
@Slf4j
public class Application {

    //TODO: All Autowired with private scope
    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private AgentConfigurationManager agentConfigurationManager;

    @Autowired
    private ApplicationContext appContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }

    @PostConstruct
    protected void init() throws ServletException {
        connectionManager.establishConnection();
        log.info(agentConfigurationManager.getAgentConfiguration().toString());


        ThreadsManager manager = (ThreadsManager) appContext.getBean("threadsManager");
        List<LogsCollectingConfiguration> logsCollectingConfigurations = new ArrayList<>();
        List<MonitoredParameterConfiguration> monitoredParameterConfigurations = new ArrayList<>();

        agentConfigurationManager.getAgentConfiguration().getServiceLogsConfigurations().stream().forEach(serviceConfiguration -> {
            serviceConfiguration.getLogsCollectingConfigurations().forEach(logsCollectingConfiguration -> {
                logsCollectingConfiguration.setServiceId(serviceConfiguration.getServiceId());
                logsCollectingConfigurations.add(logsCollectingConfiguration);
            });
            serviceConfiguration.getMonitoredParametersConfigurations().forEach(monitoredParameter -> {
                monitoredParameter.setServiceId(serviceConfiguration.getServiceId());
                monitoredParameterConfigurations.add(monitoredParameter);
            });
        });
        manager.setLogsCollectingConfigurations(logsCollectingConfigurations);
        manager.setMonitoredParameterConfigurations(monitoredParameterConfigurations);
        manager.start();


    }


}

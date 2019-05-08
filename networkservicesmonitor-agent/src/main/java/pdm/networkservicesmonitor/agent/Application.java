package pdm.networkservicesmonitor.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import pdm.networkservicesmonitor.agent.connection.ConnectionController;
import pdm.networkservicesmonitor.agent.agent_configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.model.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.agent.worker.ThreadsManager;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO: shared classes between monitor and agent - https://stackoverflow.com/questions/23704903/how-to-share-code-between-two-projects
@SpringBootApplication
@Slf4j
public class Application {

    //TODO: All Autowired with private scope
    @Autowired
    private ConnectionController connectionController;

    @Autowired
    private AgentConfigurationManager agentConfigurationManager;

    @Autowired
    private ApplicationContext appContext;

    @PostConstruct
    protected void init() throws ServletException {
        connectionController.establishConnection();
        log.info(agentConfigurationManager.getAgentConfiguration().toString());//.getLogFoldersToMonitor().stream().forEach(log::error);


        ThreadsManager manager = (ThreadsManager) appContext.getBean("threadsManager");
        List<LogsCollectingConfiguration> logsCollectingConfigurations = new ArrayList<>();
        agentConfigurationManager.getAgentConfiguration().getServiceLogsConfigurations().stream().forEach(serviceConfiguration -> {
            serviceConfiguration.getLogsCollectingConfigurations().forEach(logsCollectingConfiguration -> {
                logsCollectingConfiguration.setServiceId(serviceConfiguration.getId());
                logsCollectingConfigurations.add(logsCollectingConfiguration);
            });
        });
        manager.setLogsCollectingConfigurations(logsCollectingConfigurations);
        manager.start();



    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }






}

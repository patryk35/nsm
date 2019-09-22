package pdm.networkservicesmonitor.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.connection.ConnectionManager;
import pdm.networkservicesmonitor.agent.worker.ThreadsManager;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;

// TODO(low): shared classes between monitor and agent - https://stackoverflow.com/questions/23704903/how-to-share-code-between-two-projects
@SpringBootApplication
@Slf4j
public class AgentApplication {

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private AgentConfigurationManager agentConfigurationManager;

    @Autowired
    private ApplicationContext appContext;


    public static void main(String[] args) {

        SpringApplication.run(AgentApplication.class, args);

    }

    @PostConstruct
    protected void init() throws ServletException {
        connectionManager.establishConnection();
        log.info(agentConfigurationManager.getAgentConfiguration().toString());
        ((ThreadsManager) appContext.getBean("threadsManager")).start();
    }


}

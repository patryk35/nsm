package pdm.networkservicesmonitor.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import pdm.networkservicesmonitor.agent.configuration.AgentConfigurationManager;
import pdm.networkservicesmonitor.agent.connection.ConnectionManager;
import pdm.networkservicesmonitor.agent.payloads.data.AgentError;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;
import pdm.networkservicesmonitor.agent.worker.ThreadsManager;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@Slf4j
public class AgentApplication {

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private AgentConfigurationManager agentConfigurationManager;

    @Autowired
    private ApplicationContext appContext;

    @Setter
    @Getter
    private static ConnectionWorker connectionWorker;
    private static Queue<AgentError> agentErrorsQueue = new LinkedBlockingQueue<>();
    public static synchronized void addPacketToQueue(AgentError error) {
        agentErrorsQueue.add(error);
    }
    public static synchronized AgentError getPacketFromQueue() {
        return agentErrorsQueue.poll();
    }
    public static synchronized int getQueueSize() {
        return agentErrorsQueue.size();
    }


    public static void main(String[] args) {

        SpringApplication.run(AgentApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                log.info("Executing onExit actions ...");
                if(AgentApplication.getConnectionWorker() != null)
                    AgentApplication.getConnectionWorker().onExit();
            }
        });
    }

    @PostConstruct
    protected void init() throws ServletException {
        log.error(TimeZone.getDefault().getDisplayName());
        //TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        connectionManager.establishConnection();
        log.info(agentConfigurationManager.getAgentConfiguration().toString());
        ((ThreadsManager) appContext.getBean("threadsManager")).start();
    }


}

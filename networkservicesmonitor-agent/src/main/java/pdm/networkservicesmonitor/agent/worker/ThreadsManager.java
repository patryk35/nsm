package pdm.networkservicesmonitor.agent.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.model.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.agent.payloads.ServiceLogEntries;

import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class ThreadsManager extends Thread {

    List<LogsCollectingConfiguration> logsCollectingConfigurations;

    public void setLogsCollectingConfigurations(List<LogsCollectingConfiguration> logsCollectingConfigurations) {
        this.logsCollectingConfigurations = logsCollectingConfigurations;
    }

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    PacketManager packetManager;

    @Override
    public void run() {

        final ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) appContext.getBean("taskExecutor");
        taskExecutor.setCorePoolSize(100);
        ConnectionChecker connectionChecker = (ConnectionChecker) appContext.getBean("connectionChecker");
        taskExecutor.execute(connectionChecker);

        //PacketManager packetManager = (PacketManager) appContext.getBean("packetManager");
        taskExecutor.execute(packetManager);

        logsCollectingConfigurations.stream().forEach(l -> {
            ServiceLogEntries serviceLogEntries = new ServiceLogEntries(l.getServiceId(),l.getPath());
            int ordinal = packetManager.getServiceLogEntries().size();
            packetManager.addNewServiceLogEntries(serviceLogEntries);
            LogSWorker worker = new LogSWorker(l, ordinal);
            worker.packetManager = packetManager;
            taskExecutor.execute(worker);
        });

        for (; ; ) {
            int count = taskExecutor.getActiveCount();
            System.out.println("Active Threads : " + count);
            try {
                Thread.sleep(2000); //move ot to constants
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (count == 0) {
                taskExecutor.shutdown();
                break;
            }
        }

    }
}

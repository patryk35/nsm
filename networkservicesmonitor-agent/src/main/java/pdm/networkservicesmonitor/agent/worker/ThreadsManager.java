package pdm.networkservicesmonitor.agent.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class ThreadsManager extends Thread {

    List<String> paths;

    public void setPaths(List<String> paths) {
        this.paths = paths;
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

        paths.stream().forEach(p -> {
            log.error(p);
            LogSWorker worker = new LogSWorker(p);
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

package pdm.networkservicesmonitor.workers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.NetworkServicesMonitorApplication;

@Slf4j
@Component("webServiceWorkersManager")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WebServiceWorkersManager extends Thread {

    @Autowired
    private ApplicationContext appContext;

    @Value("${app.webservice.workers.count}")
    private int workersCount;

    @Override
    public void run() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setCorePoolSize(workersCount);
        taskExecutor.initialize();

        while (true) {
            log.info(String.format("Packets in queue: %d", NetworkServicesMonitorApplication.getQueueSize()));
            while (taskExecutor.getActiveCount() < workersCount) {
                taskExecutor.execute(appContext.getBean("webServiceWorker", WebServiceWorker.class));
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

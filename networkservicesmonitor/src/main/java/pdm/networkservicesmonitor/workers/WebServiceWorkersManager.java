package pdm.networkservicesmonitor.workers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.NetworkServicesMonitorApplication;
import pdm.networkservicesmonitor.service.SettingsService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("webServiceWorkersManager")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WebServiceWorkersManager extends Thread {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private SettingsService settingsService;

    @Override
    public void run() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setCorePoolSize(settingsService.getAppSettings().getWebserviceWorkersCount());
        taskExecutor.initialize();
        List<WebServiceWorker> workers = new ArrayList<>();
        while (true) {
            log.info(String.format("Packets in queue: %d", NetworkServicesMonitorApplication.getQueueSize()));
            while (taskExecutor.getActiveCount() < settingsService.getAppSettings().getWebserviceWorkersCount()) {
                WebServiceWorker worker = appContext.getBean("webServiceWorker", WebServiceWorker.class);
                workers.add(worker);
                executeTask(taskExecutor, worker);
            }

            for (int i = taskExecutor.getActiveCount(); i > settingsService.getAppSettings().getWebserviceWorkersCount(); i--) {
                WebServiceWorker worker = workers.get(0);
                worker.kill();
                workers.remove(worker);
            }

            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void executeTask(ThreadPoolTaskExecutor taskExecutor, Runnable runnable) {
        if (taskExecutor.getActiveCount() == taskExecutor.getCorePoolSize()) {
            taskExecutor.setCorePoolSize(taskExecutor.getCorePoolSize() * 2);
        }
        taskExecutor.execute(runnable);
    }
}

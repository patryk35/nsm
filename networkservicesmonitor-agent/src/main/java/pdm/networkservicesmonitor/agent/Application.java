package pdm.networkservicesmonitor.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pdm.networkservicesmonitor.agent.configuration.AppConfig;
import pdm.networkservicesmonitor.agent.connection.ConnectionController;
import pdm.networkservicesmonitor.agent.settings.SettingsManager;
import pdm.networkservicesmonitor.agent.worker.PrintTask2;
import pdm.networkservicesmonitor.agent.worker.ThreadsManager;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;

// TODO: shared classes between monitor and agent - https://stackoverflow.com/questions/23704903/how-to-share-code-between-two-projects
@SpringBootApplication
@Slf4j
public class Application {

    /*@Bean
    public SettingsManager settingsManager(){
        return settingsManager;
    }*/
    //TODO: All Autowired sith private scope
    @Autowired
    private ConnectionController connectionController;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private ApplicationContext appContext;

    @PostConstruct
    protected void init() throws ServletException {
        connectionController.establishConnection();
        log.info(settingsManager.getSettings().toString());//.getLogFoldersToMonitor().stream().forEach(log::error);


        ThreadsManager manager = (ThreadsManager) appContext.getBean("threadsManager");
        manager.setPaths(settingsManager.getSettings().getLogFoldersToMonitor());
        manager.start();



    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }






}

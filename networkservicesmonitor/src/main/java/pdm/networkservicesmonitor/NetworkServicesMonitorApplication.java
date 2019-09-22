package pdm.networkservicesmonitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.data.DataPacketWrapper;
import pdm.networkservicesmonitor.repository.*;
import pdm.networkservicesmonitor.service.WebServiceWorker;
import pdm.networkservicesmonitor.service.WebServiceWorkersManager;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import java.util.Queue;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@EntityScan(basePackageClasses = {
        NetworkServicesMonitorApplication.class,
        Jsr310JpaConverters.class
})

//TODO: Check jackson configuration cause it's not working properly
@Slf4j
public class NetworkServicesMonitorApplication {

    @Autowired
    private ApplicationContext appContext;

    //TODO: Move to RabbitMQ
    private static Queue<DataPacketWrapper> simpleQueue = new LinkedBlockingQueue<>();


    public static void main(String[] args) {
        SpringApplication.run(NetworkServicesMonitorApplication.class, args);
    }

    public static synchronized void addPacketToQueue(DataPacketWrapper dataPacketWrapper){
        simpleQueue.add(dataPacketWrapper);
    }

    public static synchronized DataPacketWrapper getPacketFromQueue(){
        return simpleQueue.poll();
    }

    public static synchronized int getQueueSize(){
        return simpleQueue.size();
    }


    @PostConstruct
    protected void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        //((WebServiceWorker) appContext.getBean("webServiceWorker")).start();
        ((WebServiceWorkersManager) appContext.getBean("webServiceWorkersManager")).start();

    }

}

package pdm.networkservicesmonitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import pdm.networkservicesmonitor.service.util.DataPacketWrapper;
import pdm.networkservicesmonitor.workers.AlertsWorkersManager;
import pdm.networkservicesmonitor.workers.WebServiceWorkersManager;

import javax.annotation.PostConstruct;
import java.sql.Time;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@EntityScan(basePackageClasses = {
        NetworkServicesMonitorApplication.class,
        Jsr310JpaConverters.class
})

//TODO(minor): Check jackson configuration cause it's not working properly
@Slf4j
public class NetworkServicesMonitorApplication {

    private static Queue<DataPacketWrapper> simpleQueue = new LinkedBlockingQueue<>();
    @Autowired
    private ApplicationContext appContext;

    public static void main(String[] args) {
        SpringApplication.run(NetworkServicesMonitorApplication.class, args);
    }

    public static synchronized void addPacketToQueue(DataPacketWrapper dataPacketWrapper) {
        simpleQueue.add(dataPacketWrapper);
    }

    public static synchronized DataPacketWrapper getPacketFromQueue() {
        return simpleQueue.poll();
    }

    public static synchronized int getQueueSize() {
        return simpleQueue.size();
    }

    @PostConstruct
    protected void init() {
        ((WebServiceWorkersManager) appContext.getBean("webServiceWorkersManager")).start();
        ((AlertsWorkersManager) appContext.getBean("alertsWorkersManager")).start();
    }

}

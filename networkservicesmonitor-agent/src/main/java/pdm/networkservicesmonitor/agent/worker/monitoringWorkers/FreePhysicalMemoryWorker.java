package pdm.networkservicesmonitor.agent.worker.monitoringWorkers;

import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;

import java.lang.management.ManagementFactory;
import java.sql.Timestamp;

@Slf4j
public class FreePhysicalMemoryWorker implements Runnable {

    private ConnectionWorker connectionWorker;

    private int serviceMonitoredParameterEntriesOrdinal;


    private OperatingSystemMXBean bean;
    private Long monitoringInterval;

    public FreePhysicalMemoryWorker(ConnectionWorker connectionWorker, Long monitoringInterval, int serviceMonitoredParameterEntriesOrdinal) {
        this.connectionWorker = connectionWorker;
        this.monitoringInterval = monitoringInterval;
        this.serviceMonitoredParameterEntriesOrdinal = serviceMonitoredParameterEntriesOrdinal;
        this.bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public void run() {

        while (true) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            connectionWorker.addMonitoredParameterValue("" + bean.getFreePhysicalMemorySize(), timestamp, serviceMonitoredParameterEntriesOrdinal);
            try {
                Thread.sleep(monitoringInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
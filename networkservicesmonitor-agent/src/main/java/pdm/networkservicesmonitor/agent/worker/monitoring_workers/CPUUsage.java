package pdm.networkservicesmonitor.agent.worker.monitoring_workers;

import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;

import java.lang.management.ManagementFactory;
import java.sql.Timestamp;

@Slf4j
public class CPUUsage implements Runnable {

    private ConnectionWorker connectionWorker;

    private int serviceMonitoredParameterEntriesOrdinal;


    private OperatingSystemMXBean bean;
    private Long monitoringInterval;

    public CPUUsage(ConnectionWorker connectionWorker, Long monitoringInterval, int serviceMonitoredParameterEntriesOrdinal) {
        this.connectionWorker = connectionWorker;
        this.monitoringInterval = monitoringInterval;
        this.serviceMonitoredParameterEntriesOrdinal = serviceMonitoredParameterEntriesOrdinal;
        this.bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public void run() {

        while (true) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            connectionWorker.addMonitoredParameterValue("" + bean.getSystemCpuLoad(), timestamp, serviceMonitoredParameterEntriesOrdinal);
            try {
                Thread.sleep(monitoringInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
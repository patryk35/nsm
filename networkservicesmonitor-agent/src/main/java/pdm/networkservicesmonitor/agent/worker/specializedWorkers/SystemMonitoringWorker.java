package pdm.networkservicesmonitor.agent.worker.specializedWorkers;

import com.sun.management.OperatingSystemMXBean;
import lombok.Setter;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;

import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.UUID;

public abstract class SystemMonitoringWorker extends SpecializedWorker implements Runnable {
    protected OperatingSystemMXBean bean;
    private ConnectionWorker connectionWorker;
    private UUID parameterId;
    @Setter
    private long monitoringInterval;
    private boolean enabled;


    public SystemMonitoringWorker(ConnectionWorker connectionWorker, UUID serviceId, UUID configurationId, UUID parameterId, long monitoringInterval) {
        super(serviceId, configurationId);
        this.connectionWorker = connectionWorker;
        this.serviceId = serviceId;
        this.parameterId = parameterId;
        this.monitoringInterval = monitoringInterval;
        this.bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.enabled = true;
    }

    @Override
    public void run() {

        while (enabled) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            connectionWorker.addMonitoredParameterValue(timestamp, getMonitoredValue(), serviceId, parameterId);
            try {
                Thread.sleep(monitoringInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update(long monitoringInterval) {
        this.monitoringInterval = monitoringInterval;
    }

    @Override
    public void disable() {
        enabled = false;
    }

    abstract String getMonitoredValue();
}

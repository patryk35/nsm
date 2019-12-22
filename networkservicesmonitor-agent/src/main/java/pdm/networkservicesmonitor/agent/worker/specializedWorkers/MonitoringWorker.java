package pdm.networkservicesmonitor.agent.worker.specializedWorkers;

import com.sun.management.OperatingSystemMXBean;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;
import pdm.networkservicesmonitor.agent.worker.specializedWorkers.SpecializedWorker;

import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

@Slf4j
public abstract class MonitoringWorker extends SpecializedWorker implements Runnable {
    private ConnectionWorker connectionWorker;
    private UUID parameterId;
    @Setter
    private long monitoringInterval;
    private boolean enabled;


    public MonitoringWorker(ConnectionWorker connectionWorker, UUID serviceId, UUID configurationId, UUID parameterId, long monitoringInterval) {
        super(serviceId, configurationId);
        this.connectionWorker = connectionWorker;
        this.parameterId = parameterId;
        this.monitoringInterval = monitoringInterval;
        this.enabled = true;
    }

    @Override
    public void run() {

        while (enabled) {
            Instant machineTimestamp = Instant.now();
            //Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            connectionWorker.addMonitoredParameterValue(Timestamp.from(machineTimestamp), getMonitoredValue(), serviceId, parameterId);
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

    protected abstract String getMonitoredValue();
}

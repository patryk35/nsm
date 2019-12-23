package pdm.networkservicesmonitor.agent.worker.specializedWorkers.system;

import com.sun.management.OperatingSystemMXBean;
import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;
import pdm.networkservicesmonitor.agent.worker.specializedWorkers.MonitoringWorker;

import java.lang.management.ManagementFactory;
import java.util.UUID;

public class UsedMemorySizeWorker extends MonitoringWorker {
    private OperatingSystemMXBean bean;

    public UsedMemorySizeWorker(ConnectionWorker connectionWorker, UUID serviceId, MonitoredParameterConfiguration monitoredParameterConfiguration) {
        super(connectionWorker,
                serviceId,
                monitoredParameterConfiguration.getId(),
                monitoredParameterConfiguration.getParameterId(),
                monitoredParameterConfiguration.getMonitoringInterval());
        this.bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public String getMonitoredValue() {
        return String.valueOf((bean.getTotalPhysicalMemorySize() - bean.getFreePhysicalMemorySize()));
    }
}

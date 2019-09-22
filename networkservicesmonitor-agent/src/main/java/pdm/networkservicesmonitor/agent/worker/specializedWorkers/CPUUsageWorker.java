package pdm.networkservicesmonitor.agent.worker.specializedWorkers;

import lombok.extern.slf4j.Slf4j;
import pdm.networkservicesmonitor.agent.payloads.configuration.MonitoredParameterConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;

import java.util.UUID;

@Slf4j
public class CPUUsageWorker extends SystemMonitoringWorker {


    public CPUUsageWorker(ConnectionWorker connectionWorker, UUID serviceId, MonitoredParameterConfiguration monitoredParameterConfiguration) {
        super(connectionWorker, serviceId, monitoredParameterConfiguration.getId(), monitoredParameterConfiguration.getParameterId(), monitoredParameterConfiguration.getMonitoringInterval());
    }

    @Override
    String getMonitoredValue() {
        return String.valueOf(bean.getSystemCpuLoad());
    }
}
package pdm.networkservicesmonitor.agent.payloads.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.agent.payloads.AgentToMonitorBaseRequest;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataPacket extends AgentToMonitorBaseRequest {

    @NotNull
    private Long timestamp;

    @NotNull
    private UUID packetId;

    @NotNull
    private List<ServiceLogEntries> logs;

    @NotNull
    private List<ServiceMonitoringParametersEntries> monitoring;

    public DataPacket(UUID agentId, Long timestamp, List<ServiceLogEntries> serviceLogEntries, List<ServiceMonitoringParametersEntries> monitoringEntires) {
        super(agentId);
        this.timestamp = timestamp;
        this.packetId = UUID.randomUUID();
        this.logs = new ArrayList<>();
        this.monitoring = new ArrayList<>();

        serviceLogEntries
                .forEach(sle -> {
                    ServiceLogEntries logEntries = new ServiceLogEntries(sle.getServiceId(), sle.getPath());
                    logEntries.getLogs().addAll(sle.getLogs());
                    logs.add(logEntries);
                });
        monitoringEntires.stream()
                .filter(mpe -> mpe.getMonitoredParameters().size() > 0)
                .forEach(mpe -> {
                    ServiceMonitoringParametersEntries serviceMonitoringParametersEntries = new ServiceMonitoringParametersEntries(mpe.getServiceId(), mpe.getParameterId());
                    serviceMonitoringParametersEntries.getMonitoredParameters().addAll(mpe.getMonitoredParameters());
                    monitoring.add(serviceMonitoringParametersEntries);
                });
    }

}

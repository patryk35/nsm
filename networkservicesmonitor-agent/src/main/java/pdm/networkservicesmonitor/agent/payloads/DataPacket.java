package pdm.networkservicesmonitor.agent.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DataPacket extends AgentToMonitorBaseRequest {

    @NotNull
    private Long timestamp;

    @NotNull
    private UUID packetId;

    ///TODO
    //@NotNull
    //private List<MonitoringEntries> monitoringEntries;

    @NotNull
    private List<ServiceLogEntries> logs;

    public DataPacket(UUID agentId, Long timestamp, List<ServiceLogEntries> serviceLogEntries) {
        super(agentId);
        this.timestamp = timestamp;
        this.packetId = UUID.randomUUID();
        this.logs = new ArrayList<>();
        serviceLogEntries.forEach(sle -> {
            ServiceLogEntries logEntries = new ServiceLogEntries(sle.getServiceId(),sle.getPath());
            logEntries.getLogs().addAll(sle.getLogs());
            logs.add(logEntries);
        });
    }

    public void addLogs(List<ServiceLogEntries> serviceLogEntries) {
        logs = new ArrayList<>();
        serviceLogEntries.forEach(sle -> {
            ServiceLogEntries logEntries = new ServiceLogEntries(sle.getServiceId(),sle.getPath());
            logEntries.getLogs().addAll(sle.getLogs());
        });
    }
}

package pdm.networkservicesmonitor.agent.payloads.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.agent.AgentApplication;
import pdm.networkservicesmonitor.agent.payloads.AgentToMonitorBaseRequest;
import pdm.networkservicesmonitor.agent.worker.ServiceDataEntries;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    @NotNull
    private List<AgentError> agentErrors;

    public DataPacket(UUID agentId, Long timestamp, ConcurrentHashMap<UUID, ServiceDataEntries> dataPacketEntries) {
        super(agentId);
        this.timestamp = timestamp;
        this.packetId = UUID.randomUUID();
        this.logs = new ArrayList<>();
        this.monitoring = new ArrayList<>();
        this.agentErrors = new ArrayList<>();

        dataPacketEntries.forEach((serviceId, serviceDataEntries) -> {
            serviceDataEntries.getMonitoredParameters().forEach(((parameterId, monitoredParameterEntries) -> {
                monitoring.add(new ServiceMonitoringParametersEntries(serviceId, parameterId, monitoredParameterEntries));
            }));
            serviceDataEntries.getLogs().forEach((path, logsEntries) -> {
                logs.add(new ServiceLogEntries(serviceId, path, logsEntries));
            });
            serviceDataEntries.cleanUp();
        });

        while (AgentApplication.getQueueSize() != 0){
            agentErrors.add(AgentApplication.getPacketFromQueue());
        }
    }

}

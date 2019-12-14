package pdm.networkservicesmonitor.payload.agent.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.data.AgentError;
import pdm.networkservicesmonitor.payload.agent.AgentRequest;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentDataPacket extends AgentRequest {

    @NotNull
    private Long timestamp;

    @NotNull
    private UUID packetId;

    @NotNull
    private int configurationVersion;

    @NotNull
    private List<ServiceMonitoringParameters> monitoring;

    @NotNull
    private List<ServiceLogs> logs;

    @NotNull
    private List<AgentErrorValue> agentErrors;
}

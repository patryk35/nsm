package pdm.networkservicesmonitor.payload.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.agent.data.MonitoredParameter;
import pdm.networkservicesmonitor.model.agent.data.ServiceLogs;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentDataPackage extends AgentRequest {

    @NotNull
    private Long timestamp;

    @NotNull
    private UUID packetId;

    @NotNull
    private List<ServiceLogs> serviceLogs;

    @NotNull
    private List<MonitoredParameter> monitoredParameters;
}

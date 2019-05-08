package pdm.networkservicesmonitor.payload.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    ///TODO
    //@NotNull
    //private List<MonitoringEntries> monitoringEntries;

    @NotNull
    private List<ServiceLogs> logs;
}

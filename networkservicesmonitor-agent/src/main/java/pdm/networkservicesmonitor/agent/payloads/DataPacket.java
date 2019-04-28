package pdm.networkservicesmonitor.agent.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DataPacket extends AgentToMonitorBaseRequest {
    private List<String> logs;

}

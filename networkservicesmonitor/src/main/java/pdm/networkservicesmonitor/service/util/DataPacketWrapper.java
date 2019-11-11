package pdm.networkservicesmonitor.service.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.payload.agent.packet.AgentDataPacket;

@AllArgsConstructor
public class DataPacketWrapper {
    @Getter
    private AgentDataPacket agentDataPacket;

    @Getter
    private MonitorAgent monitorAgent;
}

package pdm.networkservicesmonitor.agent.payloads;

import lombok.Getter;
import lombok.Setter;

public class UpdatesAvailabilityMonitorResponse extends MonitorToAgentBaseResponse {

    @Getter
    @Setter
    private Boolean updated;
}

package pdm.networkservicesmonitor.agent.payloads;

import lombok.Getter;
import lombok.Setter;

public class MonitorToAgentBaseResponse {
    @Getter
    @Setter
    protected Boolean success;
    @Getter
    @Setter
    protected String message;
    @Getter
    @Setter
    protected int status;
    @Getter
    @Setter
    protected String reason;
}

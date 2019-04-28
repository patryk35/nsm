package pdm.networkservicesmonitor.agent.payloads;

import lombok.Getter;
import lombok.Setter;

public class RegistrationStatusResponseToAgent extends MonitorToAgentBaseResponse {

    @Setter @Getter
    private Boolean registrationStatus;
}

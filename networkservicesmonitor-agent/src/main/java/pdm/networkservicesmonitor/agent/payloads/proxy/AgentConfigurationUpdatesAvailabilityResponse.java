package pdm.networkservicesmonitor.agent.payloads.proxy;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;

@Slf4j
@Data
@NoArgsConstructor
public class AgentConfigurationUpdatesAvailabilityResponse extends ApiResponse {

    @NotNull
    private Boolean updated;

    public AgentConfigurationUpdatesAvailabilityResponse(Boolean success, String message, HttpStatus status, Boolean updated) {
        super(success, message, status);
        this.updated = updated;
        log.debug(this.createLogMessage(String.format("updated: %s,", updated)));

    }
}

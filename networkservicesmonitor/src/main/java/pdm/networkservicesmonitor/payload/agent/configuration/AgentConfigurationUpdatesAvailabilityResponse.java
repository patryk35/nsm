package pdm.networkservicesmonitor.payload.agent.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import pdm.networkservicesmonitor.payload.client.ApiResponse;

import javax.validation.constraints.NotNull;

@Slf4j
@Data
public class AgentConfigurationUpdatesAvailabilityResponse extends ApiResponse {

    @NotNull
    private Boolean updated;

    public AgentConfigurationUpdatesAvailabilityResponse(Boolean success, String message, HttpStatus status, Boolean updated) {
        super(success, message, status);
        this.updated = updated;
        log.debug(this.createLogMessage(String.format("updated: %s,", updated)));

    }
}

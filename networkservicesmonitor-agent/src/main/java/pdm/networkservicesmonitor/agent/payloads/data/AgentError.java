package pdm.networkservicesmonitor.agent.payloads.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentError {
    @NotNull
    private Long timestamp;

    @NotNull
    private String message;
}

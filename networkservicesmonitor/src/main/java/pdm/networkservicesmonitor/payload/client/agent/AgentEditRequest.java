package pdm.networkservicesmonitor.payload.client.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AgentEditRequest {
    @NotNull
    private UUID agentId;

    @NotNull
    @Size(max = 200)
    private String description;

    @NotNull
    @Size(max = 200)
    private String allowedOrigins;

    @NotNull
    @Range(min = 1)
    private Long sendingInterval;
}

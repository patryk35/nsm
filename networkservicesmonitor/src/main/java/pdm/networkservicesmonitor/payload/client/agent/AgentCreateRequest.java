package pdm.networkservicesmonitor.payload.client.agent;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class AgentCreateRequest {
    @NotBlank
    @Size(min = 3, max = 60)
    private String name;

    @NotBlank
    @Size(min=1, max = 200)
    private String description;

    @NotNull
    @Size(max = 200)
    private String allowedOrigins;

}

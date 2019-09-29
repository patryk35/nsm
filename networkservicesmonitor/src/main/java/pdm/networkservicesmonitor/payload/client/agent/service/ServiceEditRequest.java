package pdm.networkservicesmonitor.payload.client.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceEditRequest {
    @NotNull
    private UUID serviceId;

    @NotNull
    @Size(min = 1, max = 300)
    private String description;
}

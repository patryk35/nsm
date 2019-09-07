package pdm.networkservicesmonitor.payload.client.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceAddMonitoredParameterConfigurationRequest {

    @NotNull
    private UUID serviceId;

    @NotNull
    private UUID parameterTypeId;

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotNull
    private Long monitoringInterval;
}

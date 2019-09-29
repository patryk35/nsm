package pdm.networkservicesmonitor.payload.client.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceEditMonitoredParameterConfigurationRequest {

    @NotNull
    private UUID configurationId;

    @NotBlank
    @Size(min = 1, max = 200)
    private String description;

    @NotNull
    private Long monitoringInterval;
}

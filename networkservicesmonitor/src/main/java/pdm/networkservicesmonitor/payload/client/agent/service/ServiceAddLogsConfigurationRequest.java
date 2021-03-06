package pdm.networkservicesmonitor.payload.client.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceAddLogsConfigurationRequest {

    @NotNull
    private UUID serviceId;

    @NotBlank
    private String path;

    @NotNull
    private String monitoredFilesMask;

    @NotNull
    private String logLineRegex;

}

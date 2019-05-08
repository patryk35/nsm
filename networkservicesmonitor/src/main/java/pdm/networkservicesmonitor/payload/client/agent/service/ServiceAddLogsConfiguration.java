package pdm.networkservicesmonitor.payload.client.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.ElementCollection;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceAddLogsConfiguration {

    @NotNull
    private UUID serviceId;

    @NotBlank
    private String path;

    // TODO: @NotNull
    private List<String> monitoredFilesMasks;

    // TODO: @NotNull
    private List<String> unmonitoredFileMasks;
}

package pdm.networkservicesmonitor.agent.payloads.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogsCollectingConfiguration {

    @NotNull
    private UUID id;

    @NotNull
    private UUID serviceId;

    @NotBlank
    private String path;

    @NotNull
    private List<String> monitoredFilesMasks;

    @NotNull
    private List<String> unmonitoredFileMasks;

}

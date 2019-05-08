package pdm.networkservicesmonitor.agent.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class LogsCollectingConfiguration {

    @NotNull
    private UUID id;

    private UUID serviceId;

    @NotBlank
    private String path;

    @NotNull
    private List<String> monitoredFilesMasks;

    @NotNull
    private List<String> unmonitoredFileMasks;

    public LogsCollectingConfiguration(@NotNull UUID id, @NotBlank String path, @NotNull List<String> monitoredFilesMasks, @NotNull List<String> unmonitoredFileMasks) {
        this.id = id;
        this.path = path;
        this.monitoredFilesMasks = monitoredFilesMasks;
        this.unmonitoredFileMasks = unmonitoredFileMasks;
    }
}

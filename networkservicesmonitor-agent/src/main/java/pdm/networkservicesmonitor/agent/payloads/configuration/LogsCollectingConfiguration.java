package pdm.networkservicesmonitor.agent.payloads.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogsCollectingConfiguration {

    @NotNull
    private UUID id;

    @NotBlank
    private String path;

    @NotNull
    private String monitoredFilesMask;

    @NotNull
    private String logLineRegex;

}

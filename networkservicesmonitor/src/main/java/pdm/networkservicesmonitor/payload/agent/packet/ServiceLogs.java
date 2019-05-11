package pdm.networkservicesmonitor.payload.agent.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceLogs {

    @NotNull
    private UUID serviceId;

    @NotNull
    private String path;

    @NotNull
    private List<LogEntry> logs;

}

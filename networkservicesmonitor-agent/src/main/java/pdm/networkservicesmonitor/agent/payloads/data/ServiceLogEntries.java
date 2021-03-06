package pdm.networkservicesmonitor.agent.payloads.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ServiceLogEntries {

    @NotNull
    private UUID serviceId;

    @NotNull
    private String path;

    @NotNull
    private List<LogEntry> logs;


    public ServiceLogEntries(UUID serviceId, String path, List<LogEntry> logs) {
        this.serviceId = serviceId;
        this.path = path;
        this.logs = logs;
    }
}

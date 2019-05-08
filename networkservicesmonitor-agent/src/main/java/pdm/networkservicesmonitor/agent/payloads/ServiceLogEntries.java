package pdm.networkservicesmonitor.agent.payloads;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ServiceLogEntries {

    @NotNull
    private UUID serviceId;

    @NotNull
    private String path;

    @NotNull
    private List<LogEntry> logs;


    public ServiceLogEntries(UUID serviceId, String path) {
        this.serviceId = serviceId;
        this.path = path;
        logs = new ArrayList<>();
    }
}

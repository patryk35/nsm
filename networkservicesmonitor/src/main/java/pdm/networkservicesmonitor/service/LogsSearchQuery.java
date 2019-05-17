package pdm.networkservicesmonitor.service;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
public class LogsSearchQuery {
    private String agentName;
    private UUID agentId;
    private String serviceName;
    private UUID serviceId;
    private List<String> searchedVerbs;
    private String path;
    private String querySecondPart;

    @NotNull
    private Timestamp fromTime;
    @NotNull
    private Timestamp toTime;

    public boolean valid() {
        // TODO: complex validation
        return (
                agentId != null || agentName != null
        );
    }
}

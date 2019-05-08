package pdm.networkservicesmonitor.payload.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.MonitoredParameter;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentSettingsResponse {

    private Long id;

    private Long latency = 100000L;

    private List<String> logFoldersToMonitor;

    private List<MonitoredParameter> parametersToMonitor;

}

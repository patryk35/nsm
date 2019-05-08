package pdm.networkservicesmonitor.agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.agent.settings.MonitoredParameter;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Settings {
    private Long id;

    private Long latency = 100000L;

    private List<String> logFoldersToMonitor;

    private List<MonitoredParameter> parametersToMonitor;
}

package pdm.networkservicesmonitor.agent.worker;

import lombok.Getter;
import pdm.networkservicesmonitor.agent.payloads.data.LogEntry;
import pdm.networkservicesmonitor.agent.payloads.data.MonitoredParameterEntry;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceDataEntries {

    @NotNull
    @Getter
    private ConcurrentHashMap<String, List<LogEntry>> logs;

    @NotNull
    @Getter
    private ConcurrentHashMap<UUID, List<MonitoredParameterEntry>> monitoredParameters;

    public ServiceDataEntries() {
        logs = new ConcurrentHashMap<>();
        monitoredParameters = new ConcurrentHashMap<>();
    }

    public void addLog(String path, LogEntry entry) {
        if (logs.containsKey(path)) {
            logs.get(path).add(entry);
        } else {
            logs.put(path, new ArrayList<>());
        }
    }

    public void addParameter(UUID parameterId, MonitoredParameterEntry entry) {
        if (monitoredParameters.containsKey(parameterId)) {
            monitoredParameters.get(parameterId).add(entry);
        } else {
            monitoredParameters.put(parameterId, new ArrayList<>());
        }
    }

    // TODO(medium): is removing references better than removing each value ???
    public void cleanUp() {
        logs.keySet().parallelStream().forEach(k -> {
            logs.put(k, new ArrayList<>());
        });
        monitoredParameters.keySet().parallelStream().forEach(k -> {
            monitoredParameters.put(k, new ArrayList<>());
        });
    }
}

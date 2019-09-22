package pdm.networkservicesmonitor.agent.payloads.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ServiceMonitoringParametersEntries {

    @NotNull
    private UUID serviceId;

    @NotNull
    private UUID parameterId;

    @NotNull
    private List<MonitoredParameterEntry> monitoredParameters;


    public ServiceMonitoringParametersEntries(UUID serviceId, UUID parameterId, List<MonitoredParameterEntry> monitoredParameters) {
        this.serviceId = serviceId;
        this.parameterId = parameterId;
        this.monitoredParameters = monitoredParameters;
    }
}

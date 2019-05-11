package pdm.networkservicesmonitor.agent.payloads.data;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ServiceMonitoringParametersEntries {

    @NotNull
    private UUID serviceId;

    @NotNull
    private UUID parameterId;

    @NotNull
    private List<MonitoredParameterEntry> monitoredParameters;


    public ServiceMonitoringParametersEntries(UUID serviceId, UUID parameterId) {
        this.serviceId = serviceId;
        this.parameterId = parameterId;
        monitoredParameters = new ArrayList<>();
    }
}

package pdm.networkservicesmonitor.payload.agent.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMonitoringParameters {
    @NotNull
    private UUID serviceId;

    @NotNull
    private UUID parameterId;

    @NotNull
    private List<MonitoredParameterEntry> monitoredParameters;
}

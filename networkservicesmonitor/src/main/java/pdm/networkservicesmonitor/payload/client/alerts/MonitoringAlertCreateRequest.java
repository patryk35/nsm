package pdm.networkservicesmonitor.payload.client.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.agent.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.agent.service.Service;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MonitoringAlertCreateRequest {
    @NotNull
    private UUID serviceId;
    @NotNull
    private UUID monitoredParameterTypeId;
    @NotNull
    private String message;
    @NotNull
    private String condition;
    @NotNull
    private String value;
}

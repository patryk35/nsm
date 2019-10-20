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
import javax.validation.constraints.Size;
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
    @Size(min = 3, max = 200)
    private String message;
    @NotNull
    @Size(min = 1, max = 2)
    private String condition;
    @NotNull
    @Size(min = 1, max = 200)
    private String value;
}

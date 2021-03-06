package pdm.networkservicesmonitor.payload.client.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceMonitoringConfigurationResponse {
    private UUID id;
    private String parameterName;
    private String description;
    private Long monitoringInterval;
}

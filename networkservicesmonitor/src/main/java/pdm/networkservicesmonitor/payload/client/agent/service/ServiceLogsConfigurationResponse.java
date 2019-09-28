package pdm.networkservicesmonitor.payload.client.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ServiceLogsConfigurationResponse {

    private UUID id;
    private String path;
    private String monitoredFilesMask;
    private String logLineRegex;
}

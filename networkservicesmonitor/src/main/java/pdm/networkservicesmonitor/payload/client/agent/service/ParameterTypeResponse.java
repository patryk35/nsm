package pdm.networkservicesmonitor.payload.client.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ParameterTypeResponse {

    private UUID id;
    private String name;
    private String description;
    private String targetObjectName;
}

package pdm.networkservicesmonitor.payload.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentDataPackage extends AgentRequest {
    private List<String> logs;
}

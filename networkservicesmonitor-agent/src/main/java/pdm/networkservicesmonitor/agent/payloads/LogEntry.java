package pdm.networkservicesmonitor.agent.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEntry {

    private Long timestamp;

    private String log;
}

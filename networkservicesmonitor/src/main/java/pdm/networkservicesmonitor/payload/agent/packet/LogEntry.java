package pdm.networkservicesmonitor.payload.agent.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data

@AllArgsConstructor
@NoArgsConstructor
public class LogEntry {

    private Timestamp timestamp;

    private String log;
}

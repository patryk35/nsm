package pdm.networkservicesmonitor.agent.payloads.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitoredParameterEntry {

    private Timestamp timestamp;

    private String value;
}
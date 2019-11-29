package pdm.networkservicesmonitor.payload.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitoredParameterValuesResponse {
    //TODO: Use some simpler object for it - no need to send all data
    private String name;
    private List<MonitoredParameterValue> content;
    private int dataLimit;
    private long foundDataCount;

}

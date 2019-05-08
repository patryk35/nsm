package pdm.networkservicesmonitor.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.model.agent.service.Service;

import javax.persistence.*;
import java.util.List;

@Data
@Entity(name = "agents_configurations")
public class AgentConfiguration {

    @Id
    @GeneratedValue
    private Long id;

    private Long sendingInterval;

    @JsonIgnore
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            mappedBy = "agentConfiguration")
    private MonitorAgent agent;

    public AgentConfiguration() {
        this.sendingInterval = AppConstants.AGENT_DATA_SENDING_INTERVAL;
    }
}

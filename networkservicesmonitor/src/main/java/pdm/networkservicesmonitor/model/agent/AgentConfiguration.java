package pdm.networkservicesmonitor.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import pdm.networkservicesmonitor.AppConstants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity(name = "agents_configurations")
public class AgentConfiguration {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private Long sendingInterval;

    @NotNull
    private int configurationVersion;

    @JsonIgnore
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            mappedBy = "agentConfiguration")
    private MonitorAgent agent;

    public AgentConfiguration() {
        this.sendingInterval = AppConstants.AGENT_DATA_SENDING_INTERVAL;
        this.configurationVersion = 0;
    }
}
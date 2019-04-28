package pdm.networkservicesmonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "agent_settings")
public class AgentSettings {

    @Id
    @GeneratedValue
    private Long id;

    private Long latency = 100000L;

    @ElementCollection
    private List<String> logFoldersToMonitor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "monitored_parameters",
            joinColumns = @JoinColumn(name = "agent_settings_id"),
            inverseJoinColumns = @JoinColumn(name = "monitored_parameter_id"))
    private List<MonitoredParameter> parametersToMonitor;


    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            mappedBy = "settings")
    private MonitorAgent agent;


    public AgentSettings() {
        this.logFoldersToMonitor = new ArrayList<>();
        this.parametersToMonitor = new ArrayList<>();
    }
}

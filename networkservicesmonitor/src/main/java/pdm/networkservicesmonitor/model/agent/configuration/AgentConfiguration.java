package pdm.networkservicesmonitor.model.agent.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "agents_configurations")
public class AgentConfiguration {

    @Id
    @GeneratedValue
    private Long id;

    private Long latency = 100000L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "service_logs_configurations",
            joinColumns = @JoinColumn(name = "agents_configurations_id"),
            inverseJoinColumns = @JoinColumn(name = "service_logs_configurations_id"))
    private List<ServiceLogsConfiguration> serviceLogsConfigurations;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "monitored_parameters_configurations",
            joinColumns = @JoinColumn(name = "agents_configurations_id"),
            inverseJoinColumns = @JoinColumn(name = "monitored_parameters_configurations_id"))
    private List<MonitoredParameterConfiguration> monitoredParametersConfigurations;


    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            mappedBy = "agentConfiguration")
    private MonitorAgent agent;


    public AgentConfiguration() {
        this.serviceLogsConfigurations = new ArrayList<>();
        this.monitoredParametersConfigurations = new ArrayList<>();
    }
}

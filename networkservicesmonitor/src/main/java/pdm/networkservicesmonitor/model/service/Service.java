package pdm.networkservicesmonitor.model.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.model.data.CollectedLog;
import pdm.networkservicesmonitor.model.data.MonitoredParameterValue;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "services")
@NoArgsConstructor
@Data
public class Service {
    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_id")
    private MonitorAgent agent;

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @NotNull
    @Size(min = 1, max = 300)
    private String description;

    @NotNull
    private boolean isDeleted = false;

    @NotNull
    private boolean systemService = false;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "service")
    private List<LogsCollectingConfiguration> logsCollectingConfigurations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "service")
    private List<MonitoredParameterConfiguration> monitoredParametersConfigurations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "service")
    private List<CollectedLog> collectedLogs;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "service")
    private List<MonitoredParameterValue> monitoredParameterValues;

    public Service(String name, String description, MonitorAgent agent) {
        this.name = name;
        this.description = description;
        this.agent = agent;
        logsCollectingConfigurations = new ArrayList<>();
        monitoredParametersConfigurations = new ArrayList<>();
        collectedLogs = new ArrayList<>();
        monitoredParameterValues = new ArrayList<>();
    }

}

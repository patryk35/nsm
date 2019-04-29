package pdm.networkservicesmonitor.model.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import pdm.networkservicesmonitor.model.agent.configuration.AgentConfiguration;
import pdm.networkservicesmonitor.model.audit.TimeAndUserAudit;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "agents")
public class MonitorAgent extends TimeAndUserAudit {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;

    @NotNull
    private UUID encryptionKey;

    @NotBlank
    @Size(min = 3, max = 60)
    private String name;

    @NotNull
    @Size(max = 200)
    private String description;

    @ElementCollection
    private List<String> allowedOrigins;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private AgentConfiguration agentConfiguration;

    @NotNull
    private boolean isRegistered = false;

    //TODO: Add option to enable/disable agent
    @NotNull
    private boolean isActive = false;

    @NotNull
    private boolean isConnected = false;

    public MonitorAgent(String name, String description, List<String> allowedOrigins) {
        this.name = name;
        this.description = description;
        this.allowedOrigins = allowedOrigins;
        this.encryptionKey = UUID.randomUUID();
        this.agentConfiguration = new AgentConfiguration();
    }

/*@NotBlank
    @ElementCollection
    @CollectionTable(name="labels")
    private List<String> labels;*/


}

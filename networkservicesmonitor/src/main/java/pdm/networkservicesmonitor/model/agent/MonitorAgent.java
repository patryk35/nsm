package pdm.networkservicesmonitor.model.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import pdm.networkservicesmonitor.model.service.Service;
import pdm.networkservicesmonitor.model.audit.TimeAndUserAudit;
import pdm.networkservicesmonitor.model.data.AgentError;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "agents")
@ToString(includeFieldNames = true)
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
    @Size(min = 1, max = 200)
    private String description;

    @ElementCollection
    private List<String> allowedOrigins;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private AgentConfiguration agentConfiguration;

    @NotNull
    private boolean isRegistered = false;

    @NotNull
    private boolean isDeleted = false;

    @NotNull
    private boolean isConnected = false;

    @NotNull
    private boolean isProxyAgent = false;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "agent")
    private List<AgentError> agentErrors;

    @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)
    private List<Service> services;

    public MonitorAgent(String name, String description, List<String> allowedOrigins, boolean isProxyAgent) {
        this.name = name;
        this.description = description;
        this.allowedOrigins = allowedOrigins;
        this.encryptionKey = UUID.randomUUID();
        this.agentConfiguration = new AgentConfiguration();
        this.isProxyAgent = isProxyAgent;
        services = new ArrayList<>();
    }

    public void addService(Service service) {
        services.add(service);
    }

}

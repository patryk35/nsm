package pdm.networkservicesmonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import pdm.networkservicesmonitor.model.audit.TimeAndUserAudit;

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
    private AgentSettings settings;

    @NotNull
    private boolean isRegistered = false;

    @NotNull
    private boolean isActive = false;

    @NotNull
    private boolean isConnected = false;

    public MonitorAgent(String name, String description, List<String> allowedOrigins) {
        this.name = name;
        this.description = description;
        this.allowedOrigins = allowedOrigins;
        this.encryptionKey = UUID.randomUUID();
        this.settings = new AgentSettings();
    }

/*@NotBlank
    @ElementCollection
    @CollectionTable(name="labels")
    private List<String> labels;*/


}

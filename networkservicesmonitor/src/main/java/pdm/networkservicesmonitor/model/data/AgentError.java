package pdm.networkservicesmonitor.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity(name = "agent_errors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Timestamp timestamp;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String message;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private MonitorAgent agent;

    public AgentError(Timestamp timestamp, String message, MonitorAgent agent) {
        this.message = message;
        this.timestamp = timestamp;
        this.agent = agent;
    }
}

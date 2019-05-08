package pdm.networkservicesmonitor.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import pdm.networkservicesmonitor.model.agent.service.Service;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

@Entity(name = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectedLog {
    @Id
    @GeneratedValue
    private long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @NotNull
    @Size(max = 512)
    private String path;

    @NotNull
    private Long timestamp;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String log;

    public CollectedLog(Service service, String path, Long timestamp, String log) {
        this.service = service;
        this.path = path;
        this.timestamp = timestamp;
        this.log = log;
    }
}

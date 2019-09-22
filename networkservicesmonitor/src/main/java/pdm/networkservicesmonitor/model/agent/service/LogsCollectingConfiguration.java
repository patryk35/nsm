package pdm.networkservicesmonitor.model.agent.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Entity(name = "service_logs_configurations")
@Data
@NoArgsConstructor
public class LogsCollectingConfiguration {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @NotBlank
    private String path;

    private String monitoredFilesMask;
    private String logLineRegex;


    public LogsCollectingConfiguration(String path, String monitoredFilesMask, String logLineRegex, Service service) {
        this.path = path;
        // TODO(medium): monitoredFilesMasks and unmonitoredFileMasks implementation
        this.monitoredFilesMask = monitoredFilesMask;
        this.logLineRegex = logLineRegex;
        this.service = service;
    }
}

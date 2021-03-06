package pdm.networkservicesmonitor.model.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity(name = "logs_collecting_configurations")
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

    @NotNull
    private boolean isDeleted = false;

    private String monitoredFilesMask;
    private String logLineRegex;


    public LogsCollectingConfiguration(String path, String monitoredFilesMask, String logLineRegex, Service service) {
        this.path = path;
        this.monitoredFilesMask = monitoredFilesMask;
        this.logLineRegex = logLineRegex;
        this.service = service;
    }
}

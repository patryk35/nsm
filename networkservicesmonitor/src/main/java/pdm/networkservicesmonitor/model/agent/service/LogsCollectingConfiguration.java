package pdm.networkservicesmonitor.model.agent.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
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

    //TODO: agent send to monitor all problems ( agent logs section in agent tab in client)
    @NotBlank
    private String path;

    @ElementCollection
    private List<String> monitoredFilesMasks;

    @ElementCollection
    private List<String> unmonitoredFileMasks;


    public LogsCollectingConfiguration(String path, List<String> monitoredFilesMasks, List<String> unmonitoredFileMasks, Service service) {
        this.path = path;
        // TODO: to be implemented below
        //this.monitoredFilesMasks = monitoredFilesMasks;
        //this.unmonitoredFileMasks = unmonitoredFileMasks;
        this.monitoredFilesMasks = new ArrayList<>();
        this.unmonitoredFileMasks = new ArrayList<>();
        this.service = service;
    }
}

package pdm.networkservicesmonitor.model.agent.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Entity(name = "service_logs_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceLogsConfiguration {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "uuid2")
    private UUID id;

    @NotBlank
    @Size(max = 200)
    private String serviceName;

    @ElementCollection
    private List<String> paths;

    @ElementCollection
    private List<String> filesMasks;

    @ElementCollection
    private List<String> unmonitoredFileMasks;

}

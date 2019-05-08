package pdm.networkservicesmonitor.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pdm.networkservicesmonitor.model.agent.service.Service;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name = "parameters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredParameterValue {
    @Id
    @GeneratedValue
    private long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @NotNull
    private Long timestamp;

    @NotNull
    private String value;


}

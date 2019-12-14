package pdm.networkservicesmonitor.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pdm.networkservicesmonitor.model.service.MonitoredParameterType;
import pdm.networkservicesmonitor.model.service.Service;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity(name = "collected_parameters_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredParameterValue {
    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_type_id")
    private MonitoredParameterType parameterType;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @NotNull
    private Timestamp timestamp;

    @NotNull
    private double value;

    public MonitoredParameterValue(MonitoredParameterType parameterType, Service service, @NotNull Timestamp timestamp, @NotNull double value) {
        this.parameterType = parameterType;
        this.service = service;
        this.timestamp = timestamp;
        this.value = value;
    }
}

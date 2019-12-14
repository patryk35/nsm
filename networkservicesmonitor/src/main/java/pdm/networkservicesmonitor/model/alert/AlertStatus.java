package pdm.networkservicesmonitor.model.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name = "alerts_workers_status")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertStatus {
    @NotNull
    @Id
    private String name;

    @NotNull
    private Long lastId = 0l;
}

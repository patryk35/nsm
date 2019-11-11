package pdm.networkservicesmonitor.model.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertStatus {
    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String name;

    @NotNull
    private Long lastId = 0l;
}

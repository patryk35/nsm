package pdm.networkservicesmonitor.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name = "settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {
    @Id
    @NotNull
    private String key;

    @NotNull
    private String value;
}

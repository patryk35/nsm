package pdm.networkservicesmonitor.payload.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogValue {

    @NotNull
    @Size(max = 512)
    private String path;

    @NotNull
    private Timestamp timestamp;

    @NotNull
    private String log;

    @NotNull
    private String serviceName;
}

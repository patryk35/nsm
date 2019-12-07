package pdm.networkservicesmonitor.payload.client.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    private UUID id;
    private String name;
    private String expirationTime;
    private String allowedMethods;
    private String allowedEndpoints;

}

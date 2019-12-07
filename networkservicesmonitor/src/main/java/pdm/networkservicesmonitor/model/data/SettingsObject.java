package pdm.networkservicesmonitor.model.data;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class SettingsObject {
    @NotNull
    @Min(1)
    @Max(100)
    private int webserviceWorkersCount;
    @NotNull
    @Min(1000)
    @Max(360000)
    private long alertsCheckingInterval;
    @NotNull
    private String smtpServer;
    @NotNull
    private String smtpUsername;
    @NotNull
    private String smtpPassword;
    @NotNull
    private int smtpPort;
    @NotNull
    @Email
    private String smtpFromAddress;
    @NotBlank
    private String smtpMailsFooterName;
    @NotNull
    @Min(10)
    @Max(100000)
    private int chartsMaxValuesCount;

}

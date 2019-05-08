package pdm.networkservicesmonitor.agent.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.model.Settings;

@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class SettingsManager {

    @Setter
    @Getter
    private Settings settings;



}

package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pdm.networkservicesmonitor.model.data.Settings;
import pdm.networkservicesmonitor.model.data.SettingsObject;
import pdm.networkservicesmonitor.repository.SettingsRepository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@Slf4j
public class SettingsService {
    private static SettingsObject appSettings;
    @Autowired
    private SettingsRepository settingsRepository;

    public SettingsObject getAppSettings() {
        if (appSettings == null) {
            get();
        }
        return appSettings;
    }

    public SettingsObject get() {
        SettingsObject response = new SettingsObject();
        List<Settings> settings = settingsRepository.findAll();
        settings.forEach(s -> {
            switch (s.getKey()) {
                case "app_webservice_workers_count":
                    response.setWebserviceWorkersCount(Integer.parseInt(s.getValue()));
                    break;
                case "app_alerts_checking_interval":
                    response.setAlertsCheckingInterval(Integer.parseInt(s.getValue()));
                    break;
                case "app_smtp_server":
                    response.setSmtpServer(s.getValue());
                    break;
                case "app_smtp_username":
                    response.setSmtpUsername(s.getValue());
                    break;
                case "app_smtp_password":
                    response.setSmtpPassword(s.getValue());
                    break;
                case "app_smtp_port":
                    response.setSmtpPort(Integer.parseInt(s.getValue()));
                    break;
                case "app_smtp_from_address":
                    response.setSmtpFromAddress(s.getValue());
                    break;
                case "app_charts_max_values_count":
                    response.setChartsMaxValuesCount(Integer.parseInt(s.getValue()));
                    break;
                case "app_smtp_mails_footer_name":
                    response.setSmtpMailsFooterName(s.getValue());
                    break;
                default:
                    break;
            }
        });
        appSettings = response;
        return response;
    }

    public void update(SettingsObject settingsObject) {
        List<Settings> settings = settingsRepository.findAll();
        settings.forEach(s -> {
            Optional<Settings> opt = settingsRepository.findByKey(s.getKey());
            if (opt.isPresent()) {
                Settings settingsRecord = opt.get();
                switch (s.getKey()) {
                    case "app_webservice_workers_count":
                        settingsRecord.setValue(String.valueOf(settingsObject.getWebserviceWorkersCount()));
                        s.setValue(String.valueOf(settingsObject.getWebserviceWorkersCount()));
                        break;
                    case "app_alerts_checking_interval":
                        settingsRecord.setValue(String.valueOf(settingsObject.getAlertsCheckingInterval()));
                        settingsRecord.setValue(String.valueOf(settingsObject.getAlertsCheckingInterval()));
                        break;
                    case "app_smtp_server":
                        settingsRecord.setValue(settingsObject.getSmtpServer());
                        break;
                    case "app_smtp_username":
                        settingsRecord.setValue(settingsObject.getSmtpUsername());
                        break;
                    case "app_smtp_password":
                        settingsRecord.setValue(settingsObject.getSmtpPassword());
                        break;
                    case "app_smtp_port":
                        settingsRecord.setValue(String.valueOf(settingsObject.getSmtpPort()));
                        break;
                    case "app_smtp_from_address":
                        settingsRecord.setValue(settingsObject.getSmtpFromAddress());
                        break;
                    case "app_charts_max_values_count":
                        settingsRecord.setValue(String.valueOf(settingsObject.getChartsMaxValuesCount()));
                        break;
                    case "app_smtp_mails_footer_name":
                        settingsRecord.setValue(settingsObject.getSmtpMailsFooterName());
                        break;
                    default:
                        break;
                }
                settingsRepository.save(settingsRecord);
            }
        });
        appSettings = settingsObject;
    }
}

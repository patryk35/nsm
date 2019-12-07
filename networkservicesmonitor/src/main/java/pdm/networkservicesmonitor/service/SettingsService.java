package pdm.networkservicesmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pdm.networkservicesmonitor.model.data.Settings;
import pdm.networkservicesmonitor.model.data.SettingsObject;
import pdm.networkservicesmonitor.repository.SettingsRepository;

import java.util.List;

@org.springframework.stereotype.Service
@Slf4j
public class SettingsService {
    @Autowired
    private SettingsRepository settingsRepository;

    private static SettingsObject appSettings;

    public SettingsObject getAppSettings(){
        if(appSettings == null){
            get();
        }
        return appSettings;
    }

    public SettingsObject get(){
        SettingsObject response = new SettingsObject();
        List<Settings> settings = settingsRepository.findAll();
        settings.forEach(s -> {
            switch(s.getKey()){
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

    public void update(SettingsObject settingsObject){
        List<Settings> settings = settingsRepository.findAll();
        settings.forEach(s -> {
            switch(s.getKey()){
                case "app_webservice_workers_count":
                    s.setValue(String.valueOf(settingsObject.getWebserviceWorkersCount()));
                    break;
                case "app_alerts_checking_interval":
                    s.setValue(String.valueOf(settingsObject.getAlertsCheckingInterval()));
                    break;
                case "app_smtp_server":
                    s.setValue(settingsObject.getSmtpServer());
                    break;
                case "app_smtp_username":
                    s.setValue(settingsObject.getSmtpUsername());
                    break;
                case "app_smtp_password":
                    s.setValue(settingsObject.getSmtpPassword());
                    break;
                case "app_smtp_port":
                    s.setValue(String.valueOf(settingsObject.getSmtpPort()));
                    break;
                case "app_smtp_from_address":
                    s.setValue(settingsObject.getSmtpFromAddress());
                    break;
                case "app_charts_max_values_count":
                    s.setValue(String.valueOf(settingsObject.getChartsMaxValuesCount()));
                    break;
                case "app_smtp_mails_footer_name":
                    s.setValue(settingsObject.getSmtpMailsFooterName());
                    break;
                default:
                    break;
            }
        });

    }
}

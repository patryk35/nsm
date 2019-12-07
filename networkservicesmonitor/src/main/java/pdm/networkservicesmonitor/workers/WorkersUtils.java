package pdm.networkservicesmonitor.workers;

import pdm.networkservicesmonitor.model.alert.AlertLevel;

public class WorkersUtils {
    public static String translateAlertLevel(AlertLevel level){
        switch (level){
            case INFO:
                return "Informacja";
            case WARN:
                return "Ostrzeżenie";
            case ERROR:
                return "Błąd";
            default:
                return level.name();
        }
    }
}

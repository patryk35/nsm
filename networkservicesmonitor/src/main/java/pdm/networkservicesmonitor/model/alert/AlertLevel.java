package pdm.networkservicesmonitor.model.alert;

public enum AlertLevel {
    INFO(0),
    WARN(1),
    ERROR(2);

    int level;

    AlertLevel(int level) {
        this.level = level;
    }

}

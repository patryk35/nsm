package pdm.networkservicesmonitor.model.user;

public enum MailKeyType {
    RESET(0),
    ACTIVATION(1);

    int level;

    MailKeyType(int level){
        this.level = level;
    }
}

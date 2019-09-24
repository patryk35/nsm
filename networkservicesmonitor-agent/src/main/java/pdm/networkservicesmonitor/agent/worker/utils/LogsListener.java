package pdm.networkservicesmonitor.agent.worker.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.TailerListenerAdapter;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class LogsListener extends TailerListenerAdapter {

    private ConnectionWorker connectionWorker;
    private String logLineRegex;
    private Pattern logLinePattern;
    @Getter
    private String path;
    private UUID serviceId;
    @Setter
    private boolean enabled;

    public LogsListener(ConnectionWorker connectionWorker, String logLineRegex, String path, UUID serviceId) {
        this.connectionWorker = connectionWorker;
        this.path = path;
        this.serviceId = serviceId;
        this.setLineRegex(logLineRegex);
        this.enabled = true;
    }

    public void handle(String line) {
        if (enabled && (logLineRegex.equals("") || logLinePattern.matcher(line).find())) {
            connectionWorker.addLog(serviceId, path, new Timestamp(System.currentTimeMillis()), line);
        }
    }

    public void setLineRegex(String logLineRegex) {
        this.logLineRegex = logLineRegex;
        logLinePattern = Pattern.compile(logLineRegex);
    }
}
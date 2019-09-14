package pdm.networkservicesmonitor.agent.worker.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.TailerListenerAdapter;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;

import java.sql.Timestamp;
@Slf4j
public class LogsListener extends TailerListenerAdapter {

    private ConnectionWorker connectionWorker;
    private int serviceLogEntriesOrdinal;

    public LogsListener(int serviceLogEntriesOrdinal, ConnectionWorker connectionWorker){
        this.connectionWorker = connectionWorker;
        this.serviceLogEntriesOrdinal = serviceLogEntriesOrdinal;
    }
    public void handle(String line) {
        // TODO: add logs levels to configuration  Pattern p = Pattern.compile("WARN|ERROR");
        connectionWorker.addLog(line, new Timestamp(System.currentTimeMillis()), serviceLogEntriesOrdinal);
    }
}
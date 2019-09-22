package pdm.networkservicesmonitor.agent.worker.specializedWorkers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import pdm.networkservicesmonitor.agent.payloads.configuration.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.agent.worker.ConnectionWorker;
import pdm.networkservicesmonitor.agent.worker.WorkerException;
import pdm.networkservicesmonitor.agent.worker.utils.LogsListener;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class LogWorker extends SpecializedWorker implements Runnable {
    private boolean enabled;
    private Path monitoredDirectory;
    private WatchService watchService;
    private ConnectionWorker connectionWorker;
    private String logLineRegex;
    private Pattern monitoredFilesMaskPattern;
    private List<LogsListener> logListeners;


    public LogWorker(ConnectionWorker connectionWorker, UUID serviceId, LogsCollectingConfiguration logsCollectingConfiguration) throws WorkerException {
        super(serviceId, logsCollectingConfiguration.getId());
        this.connectionWorker = connectionWorker;
        this.monitoredDirectory = Path.of(logsCollectingConfiguration.getPath());
        this.logLineRegex = logsCollectingConfiguration.getLogLineRegex();
        this.monitoredFilesMaskPattern = Pattern.compile(logsCollectingConfiguration.getMonitoredFilesMask());
        this.logListeners = new ArrayList<>();
        enabled = true;

        if (!Files.exists(monitoredDirectory)) {
            throw new WorkerException(String.format("Path %s not exists", monitoredDirectory));
        } else if (!Files.isDirectory(monitoredDirectory)) {
            throw new WorkerException(String.format("Path %s is not a directory", monitoredDirectory));
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new WorkerException(String.format("Cannot create new Watch Service for %s: \n%s", monitoredDirectory.toString(), e.getMessage()));
        }
    }

    public void update(LogsCollectingConfiguration logsCollectingConfiguration) {
        monitoredFilesMaskPattern = Pattern.compile(logsCollectingConfiguration.getMonitoredFilesMask());
        logListeners.forEach(l -> {
            l.setLineRegex(logsCollectingConfiguration.getLogLineRegex());
            if (!monitoredFilesMaskPattern.matcher(l.getPath()).find()) {
                l.setEnabled(false);
            }
        });
        try {
            initMonitoring();
        } catch (IOException e) {
            log.error(String.format("Problems with monitoring logs initialization for path %s. \n%s", monitoredDirectory, e.getMessage()));
        }
    }

    public void disable() {
        enabled = false;
    }

    public void run() {
        try {
            initMonitoring();
        } catch (IOException e) {
            log.error(String.format("Problems with monitoring logs initialization for path %s. \n%s", monitoredDirectory, e.getMessage()));
        }

        WatchKey watchKey;
        try {
            while (enabled && ((watchKey = watchService.take()) != null)) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    createTailer(monitoredDirectory.resolve((Path) event.context()));
                }
                watchKey.reset();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            log.error(String.format("Problems during monitoring logs for path %s. \n%s", monitoredDirectory, e.getMessage()));
        }

    }

    private void initMonitoring() throws IOException {
        try (DirectoryStream<Path> dirEntries = Files.newDirectoryStream(monitoredDirectory)) {
            for (Path file : dirEntries) {
                createTailer(file);
            }
        }
        monitoredDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    }

    private synchronized void createTailer(Path path) {

        if (!Files.isDirectory(path) && monitoredFilesMaskPattern.matcher(path.toString()).find()) {
            LogsListener logsListener = new LogsListener(connectionWorker, logLineRegex, path.toString(), serviceId);
            logListeners.add(logsListener);
            log.info(String.format("Creating tailer for path: %s", path));
            Tailer.create(path.toFile(), Charset.defaultCharset(), logsListener, 1000, true, true, 16384);
        }
    }


}
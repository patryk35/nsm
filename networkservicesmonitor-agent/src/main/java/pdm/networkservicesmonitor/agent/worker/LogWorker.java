package pdm.networkservicesmonitor.agent.worker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import pdm.networkservicesmonitor.agent.AppConstants;
import pdm.networkservicesmonitor.agent.payloads.configuration.LogsCollectingConfiguration;
import pdm.networkservicesmonitor.agent.worker.utils.LogsListener;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;

@Slf4j
public class LogWorker implements Runnable {

    private LogsCollectingConfiguration logsCollectingConfigurations;
    private boolean enabled;
    private Path monitoredDirectory;
    private long monitoringInterval;
    private WatchService watchService;
    private ConnectionWorker connectionWorker;
    private int serviceLogEntriesOrdinal;


    public LogWorker(ConnectionWorker connectionWorker, LogsCollectingConfiguration logsCollectingConfigurations, int serviceLogEntriesOrdinal) throws WorkerException {
        this.connectionWorker = connectionWorker;
        this.logsCollectingConfigurations = logsCollectingConfigurations;
        this.serviceLogEntriesOrdinal = serviceLogEntriesOrdinal;
        monitoredDirectory = Paths.get(logsCollectingConfigurations.getPath());
        monitoringInterval = AppConstants.LOGS_MONITORING_INTERVAL;
        // TODO: Add skipping
        enabled = true;

        if(!Files.exists(monitoredDirectory)){
            throw new WorkerException(String.format("Path %s not exists", monitoredDirectory));
        } else if (!Files.isDirectory(monitoredDirectory)) {
            throw new WorkerException(String.format("Path %s is not a directory", monitoredDirectory));
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e){
            throw new WorkerException(String.format("Cannot create new Watch Service for %s: \n%s", monitoredDirectory.toString(),e.getMessage()));
        }
    }

    public void configurationUpdate(LogsCollectingConfiguration logsCollectingConfigurations) {
        // change enable to false if configuration deleted
    }
    public void run() {
        try {
            initMonitoring();
        } catch (IOException e) {
            log.error(String.format("Problems with monitoring logs initialization for path %s. \n%s", monitoredDirectory, e.getMessage()));
        }

        WatchKey watchKey;
        try {
            while ((watchKey = watchService.take()) != null) {
                for (WatchEvent<?> event : watchKey.pollEvents()){
                    createTailer(monitoredDirectory.resolve((Path) event.context()));
                }
                watchKey.reset();
                //Thread.sleep(monitoringInterval); TODO(minor): is it necessary
            }
        } catch (InterruptedException e){
            log.error(String.format("Problems during monitoring logs for path %s. \n%s", monitoredDirectory, e.getMessage()));
        }

    }

    private void initMonitoring() throws IOException{
        try (DirectoryStream<Path> dirEntries = Files.newDirectoryStream(monitoredDirectory)) {
            //TODO: Add to configuration filter for extensions TODO(critical): Add using masks from configuration
            for (Path file : dirEntries){
                createTailer(file);
            }
        }
        monitoredDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    }

    private void createTailer(Path path) {
        if (!Files.isDirectory(path)){
            log.info(String.format("Creating tailer for path: %s", path));
            Tailer.create(path.toFile(), Charset.defaultCharset(), new LogsListener(serviceLogEntriesOrdinal, connectionWorker), 1000, true, true, 16384);
        }
    }

}
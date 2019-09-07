package pdm.networkservicesmonitor.agent.worker;

import lombok.extern.slf4j.Slf4j;
import pdm.networkservicesmonitor.agent.payloads.configuration.LogsCollectingConfiguration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class LogWorker implements Runnable {

    private ConnectionWorker connectionWorker;

    private LogsCollectingConfiguration logsCollectingConfigurations;
    private int serviceLogEntriesOrdinal;
    private Path path;
    private Map<String, Integer> lines;
    private Map<String, Integer> characters;
    private boolean enabled;


    public LogWorker(ConnectionWorker connectionWorker, LogsCollectingConfiguration logsCollectingConfigurations, int serviceLogEntriesOrdinal) {
        this.connectionWorker = connectionWorker;
        this.logsCollectingConfigurations = logsCollectingConfigurations;
        this.serviceLogEntriesOrdinal = serviceLogEntriesOrdinal;
        this.path = Paths.get(logsCollectingConfigurations.getPath());
        // TODO: Add skipping
        lines = new HashMap<>();
        characters = new HashMap<>();
        enabled = true;
        // TODO(critical): Add using masks from configuration
    }

    public void configurationUpdate(LogsCollectingConfiguration logsCollectingConfigurations) {
        // change enable to false if configuration deleted
    }

    private void loadState() {
        //TODO: Generate lines and character only at first run, next juxt load from file
    }

    public void run() {
        initCounters();
        loadState();


        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            path.toAbsolutePath().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

            WatchKey key;

            while ((key = watcher.take()) != null && enabled) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    String fileFullName = String.format("%s/%s", path.toString(), pathEvent.context().toString());
                    try (BufferedReader in = new BufferedReader(new FileReader(fileFullName))) {
                        String line;
                        //Pattern p = Pattern.compile("WARN|ERROR");
                        in.skip(characters.get(fileFullName));
                        while ((line = in.readLine()) != null) {
                            lines.replace(fileFullName, lines.get(fileFullName) + 1);
                            characters.replace(fileFullName, characters.get(fileFullName) + line.length() + System.lineSeparator().length());
                            //if (p.matcher(line).find()) {

                            if (line != null) {
                                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                connectionWorker.addLog(line, timestamp, serviceLogEntriesOrdinal);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }

                }
                key.reset();
            }


            watcher.close();

            /*do {
                WatchKey key = watcher.take();
                System.out.println("Waiting...");
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path path = pathEvent.context();
                    if (path.equals(path)) {
                        try (BufferedReader in = new BufferedReader(new FileReader(pathEvent.context().toFile()))) {
                            String line;
                            Pattern p = Pattern.compile("WARN|ERROR");
                            in.skip(characters);
                            while ((line = in.readLine()) != null) {
                                lines++;
                                characters += line.length() + System.lineSeparator().length();
                                if (p.matcher(line).find()) {
                                    // Do something
                                    System.out.println(line);
                                }
                            }
                        }
                    }
                }
                key.reset();
            } while (true);*/
        } catch (IOException | InterruptedException ex) {
            log.error(ex.getMessage());
        }
    }

    private void initCounters() {
        try (Stream<Path> paths = Files.walk(path)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(Predicate.not(f -> f.getName(0).endsWith(".gz")))
                    .forEach(f -> {
                        try (BufferedReader in = new BufferedReader(new FileReader(f.toFile()))) {
                            String line;
                            int linesCount = 0;
                            int charactersCount = 0;
                            while ((line = in.readLine()) != null) {
                                linesCount++;
                                charactersCount += line.length() + System.lineSeparator().length();
                            }
                            lines.put(f.toString(), linesCount);
                            characters.put(f.toString(), charactersCount);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            //TODO: do some logging
        }
        /*for (Map.Entry<String, Integer> entry : lines.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }*/
    }
}
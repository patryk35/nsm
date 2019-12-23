package pdm.networkservicesmonitor.workers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import pdm.networkservicesmonitor.model.alert.AlertLevel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
public class WorkersUtils {
    public static String translateAlertLevel(AlertLevel level) {
        switch (level) {
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

    public static <T> T fromJSON(final TypeReference<T> type, final String jsonPacket) {
        T data = null;
        try {
            data = new ObjectMapper().readValue(jsonPacket, type);
        } catch (Exception e) {

            log.error("Cannot load json content from file");
            log.error(e.getMessage());
        }
        return data;
    }

    public static String getFileContent(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            log.error("Cannot open file " + filePath);
            log.error(e.getMessage());
        }

        return contentBuilder.toString();
    }
}

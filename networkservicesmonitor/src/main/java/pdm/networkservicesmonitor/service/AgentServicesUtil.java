package pdm.networkservicesmonitor.service;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AgentServicesUtil {
    public static String convertOriginsToString(List<String> allowedOrigins) {
        StringBuilder sb = new StringBuilder();
        allowedOrigins.stream().forEach(o -> {
            sb.append(o);
            sb.append(", ");
        });

        if (allowedOrigins.size() > 0) {
            int trimPosition = sb.lastIndexOf(",");
            sb.deleteCharAt(trimPosition);
            sb.deleteCharAt(trimPosition);
        }
        return sb.toString();
    }

    public static List<String> convertOriginsToList(String allowedOrigins) {
        // TODO(high): should filter ip addresses and * and ip with mask
        return Pattern.compile(",").splitAsStream(allowedOrigins)
                .map(o -> o.trim())
                .filter(Predicate.not(o -> o.matches("^(|\\s+)$")))
                //.filter(o -> o.matches("^(\\*|{})"))
                .collect(Collectors.toList());
    }
}

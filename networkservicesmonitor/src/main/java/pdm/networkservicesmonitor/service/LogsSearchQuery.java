package pdm.networkservicesmonitor.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import pdm.networkservicesmonitor.exceptions.QueryException;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
public class LogsSearchQuery {
    private String agentName;
    private UUID agentId;
    private String serviceName;
    private UUID serviceId;
    private List<String> searchedVerbs;
    private String path;
    private String querySecondPart;
    private String originQueryString;

    @NotNull
    private Timestamp fromTime;
    @NotNull
    private Timestamp toTime;

    public LogsSearchQuery(String searchQuery) {
        originQueryString = searchQuery;

        String firstPart = null;
        String secondPart = null;

        try {
            Matcher queryPartsMatcher = Pattern.compile("(.*)\"(.*)").matcher(searchQuery);
            if (queryPartsMatcher.matches()) {
                firstPart = queryPartsMatcher.group(1) + "\"";
                secondPart = queryPartsMatcher.group(2);
            }
        } catch (Exception e) {
            throw new QueryException(
                    "Logs",
                    "query",
                    searchQuery,
                    "query is not valid"
            );
        }
        secondPart = secondPart.replace("\\s+=", "=").replace("=\\s+", "=");

        Matcher serviceNameMatcher = Pattern.compile(".*service=\"(.*?)\".*").matcher(firstPart);
        Matcher serviceIdMatcher = Pattern.compile(".*serviceId=\"(.*?)\".*").matcher(firstPart);
        Matcher agentNameMatcher = Pattern.compile(".*agent=\"(.*?)\".*").matcher(firstPart);
        Matcher agentIdMatcher = Pattern.compile(".*agentId=\"(.*?)\".*").matcher(firstPart);
        Matcher pathMatcher = Pattern.compile(".*path=\"(.*?)\".*").matcher(firstPart);

        if (agentNameMatcher.matches()) {
            agentName = agentNameMatcher.group(1);
        }
        if (agentIdMatcher.matches()) {
            try {
                agentId = UUID.fromString(agentIdMatcher.group(1));
            } catch (IllegalStateException e) {
                throw new QueryException(
                        "Agent Id",
                        "query",
                        searchQuery,
                        "agent not found with provided id"
                );
            }
        }
        if (serviceNameMatcher.matches()) {
            serviceName = serviceNameMatcher.group(1);
        }
        if (serviceIdMatcher.matches()) {
            try {
                serviceId = UUID.fromString(serviceIdMatcher.group(1));
            } catch (IllegalStateException e) {
                throw new QueryException(
                        "Service Id",
                        "query",
                        searchQuery,
                        "service not found with provided id"
                );
            }
        }

        if (pathMatcher.matches()) {
            path = pathMatcher.group(1);
        } else {
            path = "";
        }

        querySecondPart = secondPart.replaceFirst("\\s+", "");

    }

    @Override
    public String toString() {
        return originQueryString;
    }
}

package pdm.networkservicesmonitor.service.util;

import pdm.networkservicesmonitor.exceptions.BadRequestException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ServicesUtils {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    public static Timestamp getTimestampFromRequestDateFiled(String datetime) {
        try {
            return new Timestamp(dateFormat.parse(datetime).getTime());
        } catch (ParseException e) {
            throw new BadRequestException("Datetime format is not correct");
        }
    }

    public static void validatePageNumberAndSize(int page, int size, int maxPageSize) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > maxPageSize) {
            throw new BadRequestException("Page size cannot be greater than " + maxPageSize);
        }
    }

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

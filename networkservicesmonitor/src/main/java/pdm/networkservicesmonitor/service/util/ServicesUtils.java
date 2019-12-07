package pdm.networkservicesmonitor.service.util;

import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import pdm.networkservicesmonitor.exceptions.BadRequestException;
import pdm.networkservicesmonitor.model.alert.AlertLevel;

import javax.servlet.ServletException;
import javax.servlet.http.PushBuilder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    public static String convertListToString(List<String> list, String separator) {
        StringBuilder sb = new StringBuilder();
        list.stream().forEach(o -> {
            sb.append(o);
            sb.append(String.format("%s ", separator));
        });

        if (list.size() > 0) {
            int trimPosition = sb.lastIndexOf(separator);
            sb.deleteCharAt(trimPosition);
            sb.deleteCharAt(trimPosition);
        }
        return sb.toString();
    }

    public static List<String> convertStringToList(String text, String separator) {
        // TODO(high): For origins it should filter ip addresses and * and ip with mask
        if(text == null){
            return new ArrayList<>();
        }
        return Pattern.compile(separator).splitAsStream(text)
                .map(o -> o.trim())
                .filter(Predicate.not(o -> o.matches("^(|\\s+)$")))
                //.filter(o -> o.matches("^(\\*|{})"))
                .collect(Collectors.toList());
    }

}

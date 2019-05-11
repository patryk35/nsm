package pdm.networkservicesmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EntityScan(basePackageClasses = {
        NetworkServicesMonitorApplication.class,
        Jsr310JpaConverters.class
})
public class NetworkServicesMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetworkServicesMonitorApplication.class, args);
    }

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}

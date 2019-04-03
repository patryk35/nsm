package pdm.networkservicesmonitor.tmp;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import pdm.networkservicesmonitor.NetworkServicesMonitorApplication;

public class ServletInitializer_toberemoved extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(NetworkServicesMonitorApplication.class);
	}

}

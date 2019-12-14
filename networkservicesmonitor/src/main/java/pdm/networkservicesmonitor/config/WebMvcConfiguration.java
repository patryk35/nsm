package pdm.networkservicesmonitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Value("${app.clientURL}")
    private String clientURL;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //TODO(low): verify if it works correctly
        registry.addMapping("/**")
                //TODO: fix it .allowedOrigins(clientURL)
                .allowedOrigins("*")
                .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE")
                .maxAge(AppConstants.CORS_MAX_AGE_SECS);
    }
}
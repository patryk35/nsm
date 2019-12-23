package pdm.networkservicesmonitor.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

@Slf4j
public class AWSSecretsManagerPropertiesListener implements ApplicationListener<ApplicationPreparedEvent> {

    private final static String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
    private final static String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";
    private final static String SPRING_DATASOURCE_URL = "spring.datasource.url";
    private final static String APP_JWT_SECRET = "app.jwtSecret";

    private final static ObjectMapper mapper = new ObjectMapper();

    private static String getSecret(String secretName) {

        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .build();

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException e) {
            throw e;
        } catch (InternalServiceErrorException e) {
            throw e;
        } catch (InvalidParameterException e) {
            throw e;
        } catch (InvalidRequestException e) {
            throw e;
        } catch (ResourceNotFoundException e) {
            throw e;
        }

        if (getSecretValueResult.getSecretString() != null) {
            return getSecretValueResult.getSecretString();
        } else {
            return new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }
    }

    public static String getString(String json, String path) {
        try {
            JsonNode root = mapper.readTree(json);
            return root.path(path).asText();
        } catch (IOException e) {
            log.error("Can't get {} from json {}", path, json, e);
            return null;
        }
    }

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        String dbSecretJson = getSecret("db_credentials");
        String dbUser = getString(dbSecretJson, "username");
        String dbPassword = getString(dbSecretJson, "password");
        String url = String.format("jdbc:postgresql://%s:%s/%s",
                getString(dbSecretJson, "host"),
                getString(dbSecretJson, "port"),
                getString(dbSecretJson, "dbInstanceName")
        );

        String jwtSecretJSON = getSecret("jwtSecret");


        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        Properties props = new Properties();
        props.put(SPRING_DATASOURCE_USERNAME, dbUser);
        props.put(SPRING_DATASOURCE_PASSWORD, dbPassword);
        props.put(SPRING_DATASOURCE_URL, url);
        props.put(APP_JWT_SECRET, getString(jwtSecretJSON, "jwtSecret"));
        environment.getPropertySources().addFirst(new PropertiesPropertySource("aws.secret.manager", props));

    }


}

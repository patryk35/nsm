package pdm.networkservicesmonitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
public class MailsTemplatesConfiguration {
    @Bean
    public String passwordResetMailContentString() throws ServletException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try (Reader reader = new InputStreamReader(resourceLoader.getResource("classpath:emailTemplates/passwordReset.html").getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new ServletException("Application cannot start due to missing mail template in resource path: emailTemplates/passwordReset.html. " + e);
        }
    }

    @Bean
    public String accountActivationMailContentString() throws ServletException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try (Reader reader = new InputStreamReader(resourceLoader.getResource("classpath:emailTemplates/accountActivation.html").getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new ServletException("Application cannot start due to missing mail template in resource path: emailTemplates/accountActivation.html. " + e);
        }
    }

    @Bean
    public String accessGrantedMailContentString() throws ServletException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try (Reader reader = new InputStreamReader(resourceLoader.getResource("classpath:emailTemplates/accessGranted.html").getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new ServletException("Application cannot start due to missing mail template in resource path: emailTemplates/accessGranted.html. " + e);
        }
    }

    @Bean
    public String accessRevokedMailContentString() throws ServletException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try (Reader reader = new InputStreamReader(resourceLoader.getResource("classpath:emailTemplates/accessRevoked.html").getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new ServletException("Application cannot start due to missing mail template in resource path: emailTemplates/accessRevoked.html. " + e);
        }
    }

    @Bean
    public String alertMailContentString() throws ServletException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try (Reader reader = new InputStreamReader(resourceLoader.getResource("classpath:emailTemplates/alert.html").getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new ServletException("Application cannot start due to missing mail template in resource path: emailTemplates/alert.html. " + e);
        }
    }
}

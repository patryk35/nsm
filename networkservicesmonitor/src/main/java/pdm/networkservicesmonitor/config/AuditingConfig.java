package pdm.networkservicesmonitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pdm.networkservicesmonitor.security.UserSecurityDetails;

import java.util.Optional;

import static java.util.function.Predicate.not;


@Configuration
@EnableJpaAuditing

public class AuditingConfig {

    @Bean
    public AuditorAware<Long> auditor() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .filter(not(AnonymousAuthenticationToken.class::isInstance))
                .map(Authentication::getPrincipal)
                .map(UserSecurityDetails.class::cast)
                .map(UserSecurityDetails::getId);
    }

}

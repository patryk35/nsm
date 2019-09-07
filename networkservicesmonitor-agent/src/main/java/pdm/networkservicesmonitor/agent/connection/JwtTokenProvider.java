package pdm.networkservicesmonitor.agent.connection;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.AppConstants;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    private static String cachedToken = "";
    private static long cachedTokenExpirationTime = 0L;
    @Value("${app.jwtExpirationInMs}")
    private long validityInMilliseconds;
    @Value("${agent.id}")
    private UUID agentId;
    @Value("${agent.encryptionKey}")
    private UUID encryptionKey;
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(encryptionKey.toString().getBytes());
    }

    public String createAuthToken() {
        Date now = new Date();
        if (now.getTime() + AppConstants.TOKEN_MIN_TIME > cachedTokenExpirationTime) {
            Date validity = new Date(now.getTime() + validityInMilliseconds);
            cachedTokenExpirationTime = now.getTime() + validityInMilliseconds;
            cachedToken = Jwts.builder()
                    .setSubject(agentId.toString())
                    .setIssuedAt(now)
                    .setExpiration(validity)
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .compact();
            log.trace(String.format("Token generated %s", cachedToken));
        } else {
            log.trace(String.format("Using token from cache %s", cachedToken));
        }

        return cachedToken;
    }

}

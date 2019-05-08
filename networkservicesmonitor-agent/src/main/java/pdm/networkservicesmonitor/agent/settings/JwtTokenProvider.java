package pdm.networkservicesmonitor.agent.settings;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import pdm.networkservicesmonitor.agent.AppConstants;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwtExpirationInMs}")
    private long validityInMilliseconds;

    @Value("${agent.id}")
    private UUID agentId;

    @Value("${agent.encryptionKey}")
    private UUID encryptionKey;

    private String secretKey;

    private static String cachedToken = "";

    private static long cachedTokenExpirationTime = 0L;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(encryptionKey.toString().getBytes());
    }

    public String createAuthToken() {
        Date now = new Date();
        if(now.getTime() + AppConstants.TOKEN_MIN_TIME > cachedTokenExpirationTime){
            Date validity = new Date(now.getTime() + validityInMilliseconds);
            cachedTokenExpirationTime = now.getTime() + validityInMilliseconds;
            cachedToken= Jwts.builder()
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

    public String resolveToken(HttpServletRequest req) {

        String bearerToken = req.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            UUID agentIdFromToken = UUID.fromString(claims.getSubject());
            if(!agentIdFromToken.equals(agentId)){
                log.debug(String.format("Agent ID from token %s is not correct", token));
                return false;
            }
            log.trace(String.format("%s valid", token));
            return true;
        } catch (SignatureException ex) {
            log.debug(String.format("%s invalid signature", token));
        } catch (MalformedJwtException ex) {
            log.debug(String.format("%s token invalid", token));
        } catch (ExpiredJwtException ex) {
            log.debug(String.format("%s token expired", token));
        } catch (UnsupportedJwtException ex) {
            log.debug(String.format("%s token unsupported ", token));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug(String.format("%s token not valid", token));
        }
        return false;
    }
}

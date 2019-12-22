package pdm.networkservicesmonitor.security.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.model.agent.MonitorAgent;
import pdm.networkservicesmonitor.repository.AgentRepository;
import pdm.networkservicesmonitor.security.UserSecurityDetails;
import pdm.networkservicesmonitor.service.CustomUserDetailsService;
import static pdm.networkservicesmonitor.service.util.ServicesUtils.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${app.jwtSecret}")
    private String secretKey;

    @Value("${app.jwtExpirationInMs}")
    private long validityInMilliseconds;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String createTechnicalToken(Long userId, Date expirationTime, String name, String allowedMethods, String allowedEndpoints) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .claim("id", UUID.randomUUID())
                .claim("name", name)
                .claim("allowedMethods", allowedMethods)
                .claim("allowedEndpoints", allowedEndpoints)
                .setIssuedAt(now)
                .setExpiration(expirationTime)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserById(getUserId(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public String getExpirationTime(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().toString();
    }

    public UUID getTokenId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.get("id", String.class));
    }
    public List<String> getAllowedRequestMethods(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return convertStringToList(claims.get("allowedMethods", String.class), ",");
    }

    public List<String> getAllowedRequestEndpoints(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return convertStringToList(claims.get("allowedEndpoints", String.class), ",");
    }

    public String getTokenName(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("name", String.class);
    }

    public String resolveToken(HttpServletRequest req) {

        String bearerToken = req.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        return validateToken(token, secretKey);
    }

    public MonitorAgent validateAgentToken(String token) {
        byte[] decodedBytes = Base64.getDecoder().decode(token.split("\\.")[1]);
        String decodedString = new String(decodedBytes);
        JSONObject jsonObject = new JSONObject(decodedString);
        if(jsonObject.get("sub") == null){
            return null;
        }

        MonitorAgent agent = agentRepository.findById(UUID.fromString((String) jsonObject.get("sub")))
                .orElseThrow(() -> new NotFoundException(String.format("Agent %s not found. Agent id or encryptionKey not valid", (String) jsonObject.get("sub"))));

        boolean result = validateToken(
                token.substring(7),
                Base64.getEncoder().encodeToString(agent.getEncryptionKey().toString().getBytes())
        );

        if(result){
            return agent;
        }

        return null;
    }

    private boolean validateToken(String token, String secretKey) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
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
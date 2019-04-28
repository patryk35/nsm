package pdm.networkservicesmonitor.security.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.security.CustomUserDetailsService;
import pdm.networkservicesmonitor.security.UserSecurityDetails;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String secretKey;

    @Value("${app.jwtExpirationInMs}")
    private long validityInMilliseconds;

    @Value("${app.jwtExpirationInMsExpanded}")
    private long validityInMillisecondsExpanded;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(Authentication authentication, Boolean isRememberMeSet) {
        UserSecurityDetails userSecurityDetails = (UserSecurityDetails) authentication.getPrincipal();
        Date now = new Date();
        Date validity = new Date(now.getTime() + (isRememberMeSet ? validityInMillisecondsExpanded : validityInMilliseconds));

        return Jwts.builder()
                .setSubject(Long.toString(userSecurityDetails.getId()))
                .setIssuedAt(now)
                .setExpiration(validity)
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

    public String resolveToken(HttpServletRequest req) {

        String bearerToken = req.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    public boolean validateToken(String token){
        return validateToken(token,secretKey);
    }
    public boolean validateAgentToken(String token, UUID secretKey){
        return validateToken(
                token.substring(7, token.length()),
                Base64.getEncoder().encodeToString(secretKey.toString().getBytes())
        );
    }

    public boolean validateToken(String token, String secretKey) {
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
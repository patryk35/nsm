package pdm.networkservicesmonitor.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;
import pdm.networkservicesmonitor.exceptions.NotFoundException;
import pdm.networkservicesmonitor.exceptions.UserBadCredentialsException;
import pdm.networkservicesmonitor.model.user.User;
import pdm.networkservicesmonitor.repository.UserRepository;
import pdm.networkservicesmonitor.security.UserSecurityDetails;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            String token = tokenProvider.resolveToken(request);
            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                UsernamePasswordAuthenticationToken authentication = tokenProvider.getAuthentication(token);
                List<String> allowedMethods = tokenProvider.getAllowedRequestMethods(token);
                List<String> allowedEndpoints = tokenProvider.getAllowedRequestEndpoints(token);

                if (!allowedMethods.isEmpty() && filterList(allowedMethods, request.getMethod())) {
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            String.format("Access to method %s is forbidden with provided token.", request.getMethod())
                    );
                }

                if(!allowedEndpoints.isEmpty() && filterList(allowedEndpoints, request.getRequestURI())){
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            String.format("Access to endpoint %s is forbidden with provided token.", request.getRequestURI())
                    );
                }

                List<String> userTokens = ((UserSecurityDetails) authentication.getPrincipal()).getAccessTokens();
                if((!allowedEndpoints.isEmpty() || !allowedMethods.isEmpty()) && !userTokens.contains(token)){
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            String.format(
                                    "Token is not connected with provided user. Probably token was removed",
                                    token
                            )
                    );
                }

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private boolean filterList(List<String> list, String condition){
        return list.stream()
                .filter(e -> condition.startsWith(e))
                .collect(Collectors.toList())
                .isEmpty();
    }
}

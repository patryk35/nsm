package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pdm.networkservicesmonitor.exceptions.AppException;
import pdm.networkservicesmonitor.exceptions.UserBadCredentialsException;
import pdm.networkservicesmonitor.exceptions.UserDisabledException;
import pdm.networkservicesmonitor.model.Role;
import pdm.networkservicesmonitor.model.RoleName;
import pdm.networkservicesmonitor.model.User;
import pdm.networkservicesmonitor.payload.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.auth.AuthenticationRequest;
import pdm.networkservicesmonitor.payload.client.auth.JwtAuthenticationResponse;
import pdm.networkservicesmonitor.payload.client.auth.RegisterRequest;
import pdm.networkservicesmonitor.payload.client.auth.RegisterResponse;
import pdm.networkservicesmonitor.repository.RoleRepository;
import pdm.networkservicesmonitor.repository.UserRepository;
import pdm.networkservicesmonitor.security.jwt.JwtTokenProvider;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsernameOrEmail(),
                            authenticationRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtTokenProvider.createToken(authentication, authenticationRequest.getRememberMe());
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
        } catch (BadCredentialsException exception) {
            throw new UserBadCredentialsException("Bad credentials");
        } catch (DisabledException exception) {
            throw new UserDisabledException("User account is disabled");
        }

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return new ResponseEntity<>(new ApiBaseResponse(false, "Username is already taken!", HttpStatus.OK),
                    HttpStatus.OK);
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return new ResponseEntity<>(new ApiBaseResponse(false, "Email Address is already taken!", HttpStatus.OK),
                    HttpStatus.OK);
        }

        boolean isFirstUser = userRepository.findFirstId().isEmpty();
        User user = new User(registerRequest.getFullname(), registerRequest.getUsername(),
                registerRequest.getEmail(), registerRequest.getPassword(), isFirstUser);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (isFirstUser) {
            Role userRole = roleRepository.findByName(RoleName.ROLE_ADMINISTRATOR)
                    .orElseThrow(() -> new AppException("User Role not set."));
            user.setRoles(Collections.singleton(userRole));
        } else {
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new AppException("User Role not set."));
            user.setRoles(Collections.singleton(userRole));
        }

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        if (isFirstUser) {
            // TODO(low): Move here creating roles and monitoredPatametersTypes in DB and add creating technical user(which will be used in JwtTokenFilter) for agents
            return ResponseEntity.created(location).body(new RegisterResponse(true, "User registered successfully", HttpStatus.OK, true));

        } else {
            return ResponseEntity.created(location).body(new RegisterResponse(true, "User registered successfully", HttpStatus.OK, false));
        }
    }
}

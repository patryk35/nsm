package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import pdm.networkservicesmonitor.exceptions.AppException;
import pdm.networkservicesmonitor.exceptions.UserBadCredentialsException;
import pdm.networkservicesmonitor.exceptions.UserDisabledException;
import pdm.networkservicesmonitor.model.alert.AlertLevel;
import pdm.networkservicesmonitor.model.data.UserAlert;
import pdm.networkservicesmonitor.model.user.*;
import pdm.networkservicesmonitor.payload.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.auth.AuthenticationRequest;
import pdm.networkservicesmonitor.payload.client.auth.JwtAuthenticationResponse;
import pdm.networkservicesmonitor.payload.client.auth.RegisterRequest;
import pdm.networkservicesmonitor.payload.client.auth.RegisterResponse;
import pdm.networkservicesmonitor.repository.MailKeyRepository;
import pdm.networkservicesmonitor.repository.RoleRepository;
import pdm.networkservicesmonitor.repository.UserAlertsRepository;
import pdm.networkservicesmonitor.repository.UserRepository;
import pdm.networkservicesmonitor.security.UserSecurityDetails;
import pdm.networkservicesmonitor.security.jwt.JwtTokenProvider;
import pdm.networkservicesmonitor.service.MailingService;

import javax.validation.Valid;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    private UserAlertsRepository userAlertsRepository;

    @Autowired
    private MailKeyRepository mailKeyRepository;

    @Autowired
    private MailingService mailingService;

    @Autowired
    @Qualifier("accountActivationMailContentString")
    private String accountActivationString;

    @Value("${app.clientUserActivationCallback}")
    private String clientUserActivationCallback;

    @Value("${app.clientURL}")
    private String clientURL;

    @Value("${app.mail.appServerAddress}${app.apiUri}")
    private String apiURI;

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

            UserSecurityDetails userSecurityDetails = (UserSecurityDetails) authentication.getPrincipal();
            String jwt = jwtTokenProvider.createToken(userSecurityDetails.getId());
            //User user = userRepository.findById(((UserSecurityDetails) authentication.getPrincipal()).getId())
            //        .orElseThrow(() -> new UserBadCredentialsException("User not found!"));
            //user.getAccessTokens().add(jwt);
            //userRepository.save(user);
            // + uncomment in jwt filter
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
                registerRequest.getEmail(), registerRequest.getPassword());

        user.setUserPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));
        user.addRole(userRole);

        if (isFirstUser) {
            userRole = roleRepository.findByName(RoleName.ROLE_ADMINISTRATOR)
                    .orElseThrow(() -> new AppException("User Role not set."));
            user.addRole(userRole);
            user.setActivated(true);
            user.setEmailVerified(true);
            user.setEnabled(true);
        }

        User result = userRepository.save(user);
        MailKey key = new MailKey(user, MailKeyType.ACTIVATION);
        mailKeyRepository.save(key);

        if (!isFirstUser) {
            String content = accountActivationString
                    .replace("%link%", String.format("%s/auth/activate/%s", apiURI, key.getId().toString()))
                    .replace("%name%", user.getUsername());
            mailingService.sendMail(user.getEmail(), "Aktywacja konta", content);
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        if (isFirstUser) {
            return ResponseEntity.created(location).body(new RegisterResponse(true, "User registered successfully", HttpStatus.OK, true));

        } else {
            return ResponseEntity.created(location).body(new RegisterResponse(true, "User registered successfully", HttpStatus.OK, false));
        }
    }


    @GetMapping("/activate/{key}")
    public RedirectView activateAccount(@PathVariable("key") UUID key) {
        RedirectView redirectView = new RedirectView();
        Optional<MailKey> mailKeyOptional = mailKeyRepository.findByIdAndType(key, MailKeyType.ACTIVATION);
        if (mailKeyOptional.isEmpty()) {
            redirectView.setUrl(String.format(
                    "%s/%s/%b/%b", clientURL, clientUserActivationCallback, false, false)
            );
            return redirectView;
        }

        MailKey mailKey = mailKeyOptional.get();
        User user = mailKey.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);
        if (!user.isActivated()) {
            userAlertsRepository.save(new UserAlert(
                    user,
                    String.format("U??ytkownik o loginie %s oczekuje na aktywacj??", user.getUsername()),
                    new Timestamp(System.currentTimeMillis()),
                    AlertLevel.INFO
            ));
        }

        userRepository.save(user);
        mailKeyRepository.delete(mailKey);

        redirectView.setUrl(String.format(
                "%s/%s/%b/%b", clientURL, clientUserActivationCallback, true, user.isActivated())
        );
        return redirectView;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSecurityDetails userSecurityDetails = (UserSecurityDetails) authentication.getPrincipal();
        String jwt = jwtTokenProvider.createToken(userSecurityDetails.getId());
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

}

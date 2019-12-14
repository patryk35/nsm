package pdm.networkservicesmonitor.service;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pdm.networkservicesmonitor.config.AppConstants;
import pdm.networkservicesmonitor.exceptions.*;
import pdm.networkservicesmonitor.model.user.*;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.auth.*;
import pdm.networkservicesmonitor.repository.MailKeyRepository;
import pdm.networkservicesmonitor.repository.RoleRepository;
import pdm.networkservicesmonitor.repository.UserRepository;
import pdm.networkservicesmonitor.security.UserSecurityDetails;
import pdm.networkservicesmonitor.security.jwt.JwtTokenProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static pdm.networkservicesmonitor.service.util.ServicesUtils.convertListToString;
import static pdm.networkservicesmonitor.service.util.ServicesUtils.validatePageNumberAndSize;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailingService mailingService;

    @Autowired
    private MailKeyRepository mailKeyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    @Qualifier("passwordResetMailContentString")
    private String passwordResetString;

    @Value("${app.clientURL}")
    private String clientURL;

    @Value("${app.clientPasswordResetCallback}")
    private String clientPasswordResetCallback;

    @Autowired
    @Qualifier("accessGrantedMailContentString")
    private String accessGrantedMailContentString;

    @Autowired
    @Qualifier("accessRevokedMailContentString")
    private String accessRevokedMailContentString;

    public PagedResponse<User> getAll(int page, int size) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<User> users = userRepository.findAll(pageable);
        if (users.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), users.getNumber(),
                    users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast());
        }
        List<User> list = users.getContent();
        list.forEach(u -> u.setPassword("***"));

        return new PagedResponse<>(list, users.getNumber(),
                users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast());

    }


    public void activate(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        if (user.isActivated()) {
            throw new BadRequestException("User already activated");
        }
        user.setActivated(true);

        String content = accessGrantedMailContentString
                .replace("%name%", user.getUsername());
        mailingService.sendMail(user.getEmail(), "Nadano dostęp do konta", content);
        userRepository.save(user);
    }


    public void disable(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        if (!user.isEnabled()) {
            throw new BadRequestException("User already disabled");
        }
        user.setEnabled(false);

        String content = accessRevokedMailContentString
                .replace("%name%", user.getUsername());
        mailingService.sendMail(user.getEmail(), "Dostęp do konta został odebrany", content);
        userRepository.save(user);
    }

    public void enable(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        if (user.isEnabled()) {
            throw new BadRequestException("User already enabled");
        }
        if (getCurrentUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cannot process request. You can not change your own properties!");
        }
        user.setEnabled(true);

        String content = accessGrantedMailContentString
                .replace("%name%", user.getUsername());
        mailingService.sendMail(user.getEmail(), "Przywrócono dostęp do konta", content);
        userRepository.save(user);
    }

    public void removeAdminAccess(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        if (getCurrentUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cannot process request. You can not change your own properties!");
        }
        Role userRole = roleRepository.findByName(RoleName.ROLE_ADMINISTRATOR)
                .orElseThrow(() -> new AppException("User Role not set."));
        user.removeRole(userRole);
        userRepository.save(user);
    }

    public void addAdminAccess(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        if (getCurrentUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cannot process request. You can not change your own properties!");
        }
        Role userRole = roleRepository.findByName(RoleName.ROLE_ADMINISTRATOR)
                .orElseThrow(() -> new AppException("User Role not set."));
        user.addRole(userRole);
        userRepository.save(user);
    }

    public void removeOperatorAccess(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        if (getCurrentUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cannot process request. You can not change your own properties!");
        }
        Role userRole = roleRepository.findByName(RoleName.ROLE_OPERATOR)
                .orElseThrow(() -> new AppException("User Role not set."));
        user.removeRole(userRole);
        userRepository.save(user);
    }

    public void addOperatorAccess(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        if (getCurrentUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cannot process request. You can not change your own properties!");
        }
        Role userRole = roleRepository.findByName(RoleName.ROLE_OPERATOR)
                .orElseThrow(() -> new AppException("User Role not set."));
        user.addRole(userRole);
        userRepository.save(user);
    }


    public User get(Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        user.setPassword("***");
        user.setMailKeys(new ArrayList<>());
        return user;
    }

    public String getUserEmail() {
        return getCurrentUser().getEmail();
    }

    public void updateUserEmail(UserEmailResponse userEmailResponse) {
        User user = getCurrentUser();
        user.setEmail(userEmailResponse.getEmail());
        userRepository.save(user);
    }


    public boolean validatePassword(PasswordChangeRequest passwordResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSecurityDetails userSecurityDetails = (UserSecurityDetails) authentication.getPrincipal();
        if (passwordEncoder.matches(passwordResponse.getPassword(), userSecurityDetails.getPassword())) {
            return true;
        }
        return false;
    }

    public void updatePassword(PasswordChangeRequest passwordChangeRequest) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(passwordChangeRequest.getPassword(), user.getPassword())) {
            throw new OperationForbidden("Password is not correct!");
        }
        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);
    }

    public boolean checkUsernameAvailability(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean checkEmailAvailability(String email) {
        return !userRepository.existsByEmail(email);
    }

    public void resetPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            List<MailKey> userKeys = mailKeyRepository.findAllByUserAndType(user, MailKeyType.RESET);
            if (userKeys.size() != 0) {
                //Remove old keys if exists
                userKeys.forEach(key -> {
                    mailKeyRepository.delete(key);
                });
            }
            MailKey key = new MailKey(user, MailKeyType.RESET);
            mailKeyRepository.save(key);

            String content = passwordResetString
                    .replace("%link%", String.format("%s/%s/%s", clientURL, clientPasswordResetCallback, key.getId().toString()))
                    .replace("%name%", user.getUsername());
            mailingService.sendMail(email, "Reset hasła", content);
        }

    }

    public boolean confirmResetPassword(UUID resetKey, String password) {
        MailKey mailKey = mailKeyRepository.findByIdAndType(resetKey, MailKeyType.RESET).orElseThrow(
                () -> new ResourceNotFoundException("mailKey", "resetKey", resetKey.toString())
        );

        User user = mailKey.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        mailKeyRepository.delete(mailKey);
        return true;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSecurityDetails userSecurityDetails = (UserSecurityDetails) authentication.getPrincipal();
        return this.userRepository.findById(userSecurityDetails.getId()).orElseThrow(() -> new ResourceNotFoundException("users", "id", userSecurityDetails.getId()));
    }

    public UserDetails getCurrentUserDetails(UserSecurityDetails currentUser) {
        return new UserDetails(currentUser.getId(), currentUser.getUsername(), currentUser.getFullname(), currentUser.getAuthorities());
    }

    public AccessTokenCreateResponse addTechnicalToken(AccessToken accessToken) {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(accessToken.getExpirationTime());
        } catch (ParseException e) {
            throw new BadRequestException("Date format of expirationTime is not correct. Use format dd-MM-yyyy HH:mm:ss");
        }

        String token = tokenProvider.createTechnicalToken(
                getCurrentUser().getId(),
                date,
                accessToken.getName(),
                accessToken.getAllowedMethods(),
                accessToken.getAllowedEndpoints()
        );
        User user = userRepository.findById(getCurrentUser().getId()).orElseThrow(
                () -> new UserBadCredentialsException("User not found")
        );
        user.getAccessTokens().add(token);
        userRepository.save(user);
        return new AccessTokenCreateResponse(token);
    }

    public List<AccessToken> getUserTechnicalTokens() {
        List<AccessToken> accessTokens = new ArrayList<>();
        getCurrentUser().getAccessTokens().forEach(t -> {
            try{
                accessTokens.add(new AccessToken(
                        tokenProvider.getTokenId(t),
                        tokenProvider.getTokenName(t),
                        tokenProvider.getExpirationTime(t),
                        convertListToString(tokenProvider.getAllowedRequestMethods(t), ","),
                        convertListToString(tokenProvider.getAllowedRequestEndpoints(t), ",")
                ));
            } catch (ExpiredJwtException e) {
                User user = userRepository.findById(getCurrentUser().getId()).orElseThrow(
                        () -> new UserBadCredentialsException("User not found")
                );
                // TODO(medium): fix below
                // Removing expired tokens
                //user.getAccessTokens().remove(user.getAccessTokens().indexOf(t));
                //userRepository.save(user);
            }
        });

        return accessTokens;
    }

    public void deleteUserToken(UUID tokenId){
        User user = userRepository.findById(getCurrentUser().getId()).orElseThrow(
                () -> new UserBadCredentialsException("User not found")
        );
        getCurrentUser().getAccessTokens().stream()
                .filter(t -> tokenProvider.getTokenId(t).equals(tokenId))
                .collect(Collectors.toList())
                .forEach(t -> user.getAccessTokens().remove(t));
        userRepository.save(user);

    }
}

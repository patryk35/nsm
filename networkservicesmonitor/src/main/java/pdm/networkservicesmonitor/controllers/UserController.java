package pdm.networkservicesmonitor.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.exceptions.MethodNotAllowed;
import pdm.networkservicesmonitor.exceptions.OperationForbidden;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.User;
import pdm.networkservicesmonitor.payload.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.auth.DataAvailability;
import pdm.networkservicesmonitor.payload.client.auth.UserDetails;
import pdm.networkservicesmonitor.payload.client.auth.UserEmailResponse;
import pdm.networkservicesmonitor.payload.client.auth.PasswordChangeRequest;
import pdm.networkservicesmonitor.repository.UserRepository;
import pdm.networkservicesmonitor.security.UserSecurityDetails;

import javax.servlet.UnavailableException;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static pdm.networkservicesmonitor.service.util.ServicesUtils.validatePageNumberAndSize;

@RestController
@RequestMapping("${app.apiUri}/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public PagedResponse<User> getAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        validatePageNumberAndSize(page, size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<User> users = userRepository.findAll(pageable);
        if (users.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), users.getNumber(),
                    users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast());
        }
        List<User> list = users.getContent();
        list.stream().forEach(u -> u.setPassword("***"));

        return new PagedResponse<>(list, users.getNumber(),
                users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast());

    }

    @PostMapping("/activate/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse activate(@PathVariable("id") Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        user.setIsEnabled(true);
        userRepository.save(user);
        return new ApiBaseResponse(true,"Successfully activated", HttpStatus.OK);
    }

    @PostMapping("/deactivate/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse deactivate(@PathVariable("id") Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        user.setIsEnabled(false);
        userRepository.save(user);
        return new ApiBaseResponse(true,"Successfully deactivated", HttpStatus.OK);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public User get(@PathVariable("id") Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        user.setPassword("***");
        return user;
    }

    @GetMapping("/email")
    public UserEmailResponse getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSecurityDetails userSecurityDetails = (UserSecurityDetails) authentication.getPrincipal();
        User user = this.userRepository.findById(userSecurityDetails.getId()).orElseThrow(() -> new ResourceNotFoundException("users", "id", userSecurityDetails.getId()));
        return new UserEmailResponse(user.getEmail());
    }

    @PatchMapping("/email")
    public ApiBaseResponse updateUserEmail(@Valid @RequestBody UserEmailResponse userEmailResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSecurityDetails userSecurityDetails = (UserSecurityDetails) authentication.getPrincipal();
        User user = this.userRepository.findById(userSecurityDetails.getId()).orElseThrow(() -> new ResourceNotFoundException("users", "id", userSecurityDetails.getId()));
        user.setEmail(userEmailResponse.getEmail());
        userRepository.save(user);
        return new ApiBaseResponse(true,"Successfully updated", HttpStatus.OK);
    }


    @PostMapping("/password/validate")
    public ApiBaseResponse validatePassword(@Valid @RequestBody PasswordChangeRequest passwordResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSecurityDetails userSecurityDetails = (UserSecurityDetails) authentication.getPrincipal();
        if(passwordEncoder.matches(passwordResponse.getPassword(), userSecurityDetails.getPassword())){
            return new ApiBaseResponse(true, "Password is valid!", HttpStatus.OK);
        }
        return new ApiBaseResponse(false, "Password is invalid!", HttpStatus.OK);
    }

    @PatchMapping("/password")
    public ApiBaseResponse updatePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSecurityDetails userSecurityDetails = (UserSecurityDetails) authentication.getPrincipal();
        User user = this.userRepository.findById(userSecurityDetails.getId()).orElseThrow(() -> new ResourceNotFoundException("users", "id", userSecurityDetails.getId()));
        if(!passwordEncoder.matches(passwordChangeRequest.getPassword(),userSecurityDetails.getPassword())){
            throw new OperationForbidden("Password is not correct!");
        }
        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);
        return new ApiBaseResponse(true,"Successfully updated", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        throw new MethodNotAllowed("User cannot be deleted. Try to disable user");
    }

    @GetMapping("/details")
    public UserDetails getCurrentUserDetails(@AuthenticationPrincipal UserSecurityDetails currentUser) {
        UserDetails userDetails = new UserDetails(currentUser.getId(), currentUser.getUsername(), currentUser.getFullname(), currentUser.getAuthorities());
        return userDetails;
    }

    @GetMapping("/getUsernameAvailability")
    public DataAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        return new DataAvailability(!userRepository.existsByUsername(username), true, "", HttpStatus.OK);
    }

    @GetMapping("/getEmailAvailability")
    public DataAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
        return new DataAvailability(!userRepository.existsByEmail(email), true, "", HttpStatus.OK);
    }

}
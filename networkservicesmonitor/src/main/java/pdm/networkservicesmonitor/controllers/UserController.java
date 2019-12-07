package pdm.networkservicesmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import pdm.networkservicesmonitor.AppConstants;
import pdm.networkservicesmonitor.exceptions.BadRequestException;
import pdm.networkservicesmonitor.exceptions.MethodNotAllowed;
import pdm.networkservicesmonitor.model.user.User;
import pdm.networkservicesmonitor.payload.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.client.PagedResponse;
import pdm.networkservicesmonitor.payload.client.auth.*;
import pdm.networkservicesmonitor.security.UserSecurityDetails;
import pdm.networkservicesmonitor.service.UserService;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("${app.apiUri}/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${app.clientURL}")
    private String clientURL;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public PagedResponse<User> getAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        return userService.getAll(page, size);

    }

    @PostMapping("/activate/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse activate(@PathVariable("id") Long id) {
        userService.activate(id);
        return new ApiBaseResponse(true, "Successfully activated", HttpStatus.OK);
    }

    @PostMapping("/enable/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse enable(@PathVariable("id") Long id) {
        userService.enable(id);
        return new ApiBaseResponse(true, "Successfully enabled", HttpStatus.OK);
    }

    @PostMapping("/disable/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse disable(@PathVariable("id") Long id) {
        userService.disable(id);
        return new ApiBaseResponse(true, "Successfully disabled", HttpStatus.OK);
    }

    @PostMapping("admin/enable/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse addAdminAccess(@PathVariable("id") Long id) {
        userService.addAdminAccess(id);
        return new ApiBaseResponse(true, "Successfully disabled", HttpStatus.OK);
    }

    @PostMapping("/admin/disable/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse removeAdminAccess(@PathVariable("id") Long id) {
        userService.removeAdminAccess(id);
        return new ApiBaseResponse(true, "Successfully disabled", HttpStatus.OK);
    }

    @PostMapping("operator/enable/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse addOperatorAccess(@PathVariable("id") Long id) {
        userService.addOperatorAccess(id);
        return new ApiBaseResponse(true, "Successfully disabled", HttpStatus.OK);
    }

    @PostMapping("/operator/disable/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiBaseResponse removeOperatorAccess(@PathVariable("id") Long id) {
        userService.removeOperatorAccess(id);
        return new ApiBaseResponse(true, "Successfully disabled", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public User get(@PathVariable("id") Long id) {
        return userService.get(id);
    }

    @GetMapping("/email")
    public UserEmailResponse getUserEmail() {
        return new UserEmailResponse(userService.getUserEmail());
    }

    @PatchMapping("/email")
    public ApiBaseResponse updateUserEmail(@Valid @RequestBody UserEmailResponse userEmailResponse) {
        userService.updateUserEmail(userEmailResponse);
        return new ApiBaseResponse(true, "Successfully updated", HttpStatus.OK);
    }


    @PostMapping("/password/validate")
    public ApiBaseResponse validatePassword(@Valid @RequestBody PasswordChangeRequest passwordResponse) {
        if (userService.validatePassword(passwordResponse)) {
            return new ApiBaseResponse(true, "Password is valid!", HttpStatus.OK);
        }
        return new ApiBaseResponse(false, "Password is invalid!", HttpStatus.OK);
    }

    @PatchMapping("/password")
    public ApiBaseResponse updatePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        userService.updatePassword(passwordChangeRequest);
        return new ApiBaseResponse(true, "Successfully updated", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        throw new MethodNotAllowed("User cannot be deleted. Try to disable user");
    }

    @GetMapping("/details")
    public UserDetails getCurrentUserDetails(@AuthenticationPrincipal UserSecurityDetails currentUser) {
        return userService.getCurrentUserDetails(currentUser);
    }

    @GetMapping("/getUsernameAvailability")
    public DataAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        return new DataAvailability(userService.checkUsernameAvailability(username), true, "", HttpStatus.OK);
    }

    @GetMapping("/getEmailAvailability")
    public DataAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
        return new DataAvailability(userService.checkEmailAvailability(email), true, "", HttpStatus.OK);
    }

    @PostMapping("/resetPassword")
    public ApiBaseResponse resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        userService.resetPassword(passwordResetRequest.getEmail());
        return new ApiBaseResponse(true, "Successfully updated", HttpStatus.OK);
    }

    @PostMapping("/resetPassword/confirm")
    public ApiBaseResponse confirmResetPassword(@Valid @RequestBody PasswordResetConfirmRequest passwordResetConfirmRequest) {
        userService.confirmResetPassword(
                passwordResetConfirmRequest.getResetKey(),
                passwordResetConfirmRequest.getPassword()
        );
        return new ApiBaseResponse(true, "Successfully updated", HttpStatus.OK);
    }

    @PostMapping("/token")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public AccessTokenCreateResponse addTechnicalToken(@Valid @RequestBody AccessToken accessToken) {
        return userService.addTechnicalToken(accessToken);
    }

    @GetMapping("/token")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public List<AccessToken> getUserTechnicalTokens() {
        return userService.getUserTechnicalTokens();

    }

    @DeleteMapping("/token/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('OPERATOR')")
    public ResponseEntity<?> deleteUserToken(@PathVariable UUID id) {
        userService.deleteUserToken(id);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/").build().toUri();

        return ResponseEntity.created(location)
                .body(new ApiBaseResponse(true, "Token deleted successfully", HttpStatus.OK));
    }

}
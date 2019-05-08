package pdm.networkservicesmonitor.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pdm.networkservicesmonitor.exceptions.AppNotImplementedException;
import pdm.networkservicesmonitor.exceptions.MethodNotAllowed;
import pdm.networkservicesmonitor.exceptions.ResourceNotFoundException;
import pdm.networkservicesmonitor.model.User;
import pdm.networkservicesmonitor.payload.client.auth.DataAvailability;
import pdm.networkservicesmonitor.payload.client.auth.UserDetails;
import pdm.networkservicesmonitor.repository.UserRepository;
import pdm.networkservicesmonitor.security.UserSecurityDetails;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("${app.apiUri}/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity getAll() {
        List<User> users = userRepository.findAll();
        users.stream().forEach(u -> u.setPassword("***"));
        return ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public User get(@PathVariable("id") Long id) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("users", "id", id));
        user.setPassword("***");
        return user;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        throw new MethodNotAllowed("User cannot be deleted. Try to disable user");
    }

    // TODO(MEDIUM): Admin add user, disable registration after adding first user
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity add() {
        throw new AppNotImplementedException("Add User", "MEDIUM; to be done later");
    }

    // TODO(MEDIUM): Admin edit user data
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity edit(@PathVariable("id") Long id) {
        throw new AppNotImplementedException("Edit User", "MEDIUM; to be done later");
    }

    // TODO(MEDIUM): User edit his data
    @PutMapping("/details/{id}")
    public ResponseEntity update(@PathVariable("id") Long id) {
        throw new AppNotImplementedException("Edit User", "MEDIUM; to be done later");
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
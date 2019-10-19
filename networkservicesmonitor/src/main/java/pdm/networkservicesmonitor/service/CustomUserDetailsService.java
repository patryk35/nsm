package pdm.networkservicesmonitor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pdm.networkservicesmonitor.model.user.User;
import pdm.networkservicesmonitor.repository.UserRepository;
import pdm.networkservicesmonitor.security.UserSecurityDetails;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Comment about: Allows users to log using both login and email
     *
     * @param usernameOrEmail
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format(
                                "User not found. Please provide correct username or email : %s",
                                usernameOrEmail
                        ))
                );

        return UserSecurityDetails.create(user);
    }

    /**
     * This method is used by JWTAuthenticationFilter
     *
     * @param id
     * @return
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User not found with id : %s",id))
        );

        return UserSecurityDetails.create(user);
    }
}
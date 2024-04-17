package account.service;

import account.exception.BruteForceException;
import account.exception.LoginFailedException;
import account.model.AuthUser;
import account.model.Role;
import account.repository.UserRepository;
import account.security.AuthUserDetails;
import account.security.LoginAttemptService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  private final EventService eventService;

  private final LoginAttemptService loginAttemptService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//    if (loginAttemptService.isBlocked(username)) {
//      log.error("The username {} has been blocked.", username);
//      throw new BruteForceException("Blocked");
//    }

    Optional<AuthUser> user = userRepository.findByEmail(username.toLowerCase());
    if (user.isEmpty()) {
      log.error("The username has been not found.");
      throw new LoginFailedException("The username has been not found.", username);
    }

    log.info("Retrieving the user: {}", user.get());
    AuthUserDetails.AuthUserDetailsBuilder authUserDetailsBuilder = AuthUserDetails.builder();
    authUserDetailsBuilder.username(user.get().getEmail())
        .password(user.get().getPassword())
        .unlock(user.get().isUnlock());
    List<Role> roles = user.get().getRoles();
    Collection<GrantedAuthority> authorities = new ArrayList<>(roles.size());
    for (Role element : roles) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + element.getRole().toUpperCase()));
    }
    log.info("The authorized roles are: {}", authorities);
    authUserDetailsBuilder.authorities(authorities);

    return authUserDetailsBuilder.build();
  }
}

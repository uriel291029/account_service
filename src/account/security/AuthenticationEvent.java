package account.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationEvent {

  private final LoginAttemptService loginAttemptService;
  @EventListener
  public void onSuccess(AuthenticationSuccessEvent success) {
    log.info("Success Login with: {}", success.getAuthentication());
    String username = success.getAuthentication().getName();
    loginAttemptService.unLockUser(username);
  }
}

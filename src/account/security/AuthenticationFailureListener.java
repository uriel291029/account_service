package account.security;

import account.utils.AuthorizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationFailureListener implements
    ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private LoginAttemptService loginAttemptService;

  @Override
  public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
    final String xfHeader = request.getHeader("X-Forwarded-For");
    //log.info("X-Forwarded-For: {}", xfHeader);
    //if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
    log.info("Authentication:{}", e.getAuthentication());
    final String authorization = request.getHeader("authorization");
      String username = AuthorizationUtil.getUsernameFromAuthHeader(authorization);
      loginAttemptService.loginFailed(username);
   // } else {
   //   loginAttemptService.loginFailed(xfHeader.split(",")[0]);
   // }
  }
}
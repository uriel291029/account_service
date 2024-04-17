package account.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoginAttemptService {

  public static final int MAX_ATTEMPT = 5;
  private LoadingCache<String, Integer> attemptsCache;

  @Autowired
  private HttpServletRequest request;

  public LoginAttemptService() {
    super();
    attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS)
        .build(new CacheLoader<String, Integer>() {
          @Override
          public Integer load(final String key) {
            return 0;
          }
        });
  }

  public void loginFailed(final String key) {
    int attempts;
    try {
      attempts = attemptsCache.get(key);
    } catch (final ExecutionException e) {
      attempts = 0;
    }
    attempts++;
    attemptsCache.put(key, attempts);
    log.info("The username {} in the service has tried login {} attempts", key, attempts);
  }

  public boolean isBlocked(String username) {
    try {
      log.info("The username {} in the service with {} attempts"
              + " has been blocked: {}", username, attemptsCache.get(username),
          attemptsCache.get(username) >= MAX_ATTEMPT);
      return attemptsCache.get(username) >= MAX_ATTEMPT;
    } catch (final ExecutionException e) {
      return false;
    }
  }

  public void unLockUser(String username) {
    attemptsCache.put(username, 0);
  }

  private String getClientIP() {
    final String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader != null) {
      return xfHeader.split(",")[0];
    }
    return request.getRemoteAddr();
  }
}

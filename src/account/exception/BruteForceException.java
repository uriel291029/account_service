package account.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class BruteForceException extends UsernameNotFoundException {

  public BruteForceException(String message) {
    super(message);
  }
}

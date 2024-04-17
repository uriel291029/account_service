package account.exception;

import lombok.Getter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Getter
public class LoginFailedException extends UsernameNotFoundException {

  private String username;
  private String action;

  public LoginFailedException(String msg, String username) {
    super(msg);
    this.username = username;
  }
}

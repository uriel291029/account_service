package account.security;

import account.exception.BadRequestException;
import account.repository.PasswordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordValidator {

  private final PasswordEncoder passwordEncoder;

  private final PasswordRepository passwordRepository;

  public void validNewPasswordWithPreviousOne(String newPassword, String hashedOldPassword) {
    if (passwordEncoder.matches(newPassword, hashedOldPassword)) {
      log.error("The passwords must be different.");
      throw new BadRequestException("The passwords must be different!");
    }
  }

  public void validPasswordWithBreachedPasswords(String password) {
    if (passwordRepository.getBreachedPassWords().contains(password)) {
      log.error("The password is in the hacker's database.");
      throw new BadRequestException("The password is in the hacker's database!");
    }
  }
}

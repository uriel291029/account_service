package account.service;

import account.dto.request.ChangePassRequest;
import account.dto.response.ChangePasswordResponse;
import account.exception.NotFoundException;
import account.model.AuthUser;
import account.repository.UserRepository;
import account.security.PasswordValidator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final PasswordValidator passwordValidator;

  public ChangePasswordResponse changePassword(ChangePassRequest changePassRequest) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    log.info("Retrieving the user with the email: {}", authentication.getName());
    Optional<AuthUser> optionalUser = userRepository.findByEmail(
        authentication.getName().toLowerCase());
    if (optionalUser.isEmpty()) {
      throw new NotFoundException("");
    }
    AuthUser authUser = optionalUser.get();

    log.info("Validating the password.");
    passwordValidator.validPasswordWithBreachedPasswords(changePassRequest.getNewPassword());
    passwordValidator.validNewPasswordWithPreviousOne(changePassRequest.getNewPassword(),
        authUser.getPassword());

    authUser.setPassword(passwordEncoder.encode(changePassRequest.getNewPassword()));
    userRepository.save(authUser);

    ChangePasswordResponse changePasswordResponse = ChangePasswordResponse.builder()
        .email(authUser.getEmail())
        .status("The password has been updated successfully").build();
    log.info("The password has been updated successfully");
    return changePasswordResponse;
  }
}

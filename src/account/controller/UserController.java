package account.controller;

import account.dto.request.ChangePassRequest;
import account.dto.request.EventRequest;
import account.dto.request.UserRequest;
import account.dto.response.ChangePasswordResponse;
import account.dto.response.UserResponse;
import account.service.EventService;
import account.service.PasswordService;
import account.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api")
public class UserController {

  private final UserService userService;

  private final PasswordService passwordService;

  private final EventService eventService;

  @PostMapping("auth/signup")
  public UserResponse registerUser(@Valid @RequestBody UserRequest userRequest,
      HttpServletRequest httpServletRequest) {
    UserResponse userResponse = userService.registerUser(userRequest);
    EventRequest eventRequest = EventRequest.builder()
        .localDateTime(LocalDateTime.now())
        .path(httpServletRequest.getRequestURI())
        .subject("Anonymous")
        .action("CREATE_USER")
        .object(userResponse.getEmail()).build();
    eventService.registerEvent(eventRequest);
    return userResponse;
  }

  @PostMapping("auth/changepass")
  public ChangePasswordResponse changePass(@Valid @RequestBody ChangePassRequest changePassRequest,
      HttpServletRequest httpServletRequest, @AuthenticationPrincipal UserDetails userDetails) {
    ChangePasswordResponse changePasswordResponse = passwordService.changePassword(
        changePassRequest);
    EventRequest eventRequest = EventRequest.builder()
        .localDateTime(LocalDateTime.now())
        .path(httpServletRequest.getRequestURI())
        .subject(userDetails.getUsername())
        .action("CHANGE_PASSWORD")
        .object(httpServletRequest.getUserPrincipal().getName()).build();
    eventService.registerEvent(eventRequest);
    return changePasswordResponse;
  }
}

package account.controller;

import account.dto.request.EventRequest;
import account.dto.request.LockUserPutRequest;
import account.dto.request.RolePutRequest;
import account.dto.response.LockUserPutResponse;
import account.dto.response.RolePutResponse;
import account.dto.response.UserDeleteResponse;
import account.dto.response.UserResponse;
import account.service.EventService;
import account.service.PasswordService;
import account.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api")
public class AdministratorController {

  private final UserService userService;

  private final EventService eventService;

  @RolesAllowed({"ROLE_ADMINISTRATOR"})
  @GetMapping("admin/user/")
  public List<UserResponse> retrieveUsers() {
    return userService.retrieveUsers();
  }


  @RolesAllowed({"ROLE_ADMINISTRATOR"})
  @PutMapping("admin/user/role")
  public RolePutResponse updateUserRole(@Valid @RequestBody RolePutRequest rolePutRequest,
      HttpServletRequest httpServletRequest, @AuthenticationPrincipal UserDetails userDetails) {
    log.info("Consuming the service admin/user/role");
    RolePutResponse rolePutResponse = userService.updateRolesInUser(rolePutRequest);
    EventRequest.EventRequestBuilder eventRequestBuilder = EventRequest.builder();
    EventRequest eventRequest = switch (rolePutRequest.getOperation()) {
      case "GRANT" -> eventRequestBuilder.localDateTime(LocalDateTime.now())
          .path(httpServletRequest.getRequestURI())
          .subject(userDetails.getUsername())
          .action("GRANT_ROLE")
          .object(String.format("Grant role %s to %s", rolePutRequest.getRole(),
              rolePutResponse.getEmail())).build();
      case "REMOVE" -> eventRequestBuilder.localDateTime(LocalDateTime.now())
          .path(httpServletRequest.getRequestURI())
          .subject(userDetails.getUsername())
          .action("REMOVE_ROLE")
          .object(String.format("Remove role %s from %s", rolePutRequest.getRole(),
              rolePutResponse.getEmail())).build();
      default -> null;
    };
    eventService.registerEvent(eventRequest);
    return rolePutResponse;
  }

  @RolesAllowed({"ROLE_ADMINISTRATOR"})
  @DeleteMapping("admin/user/{email}")
  public UserDeleteResponse removeUser(@PathVariable String email,
      HttpServletRequest httpServletRequest, @AuthenticationPrincipal UserDetails userDetails) {
    log.info("Removing the user: {}", email);
    UserDeleteResponse userDeleteResponse = userService.deleteUser(email);
    EventRequest eventRequest = EventRequest.builder()
        .localDateTime(LocalDateTime.now())
        .path(httpServletRequest.getRequestURI())
        .subject(userDetails.getUsername())
        .action("DELETE_USER")
        .object(email).build();
    eventService.registerEvent(eventRequest);
    return userDeleteResponse;
  }

  @RolesAllowed({"ROLE_ADMINISTRATOR"})
  @PutMapping("admin/user/access")
  public LockUserPutResponse updateUserAccess(
      @Valid @RequestBody LockUserPutRequest lockUserPutRequest,
      HttpServletRequest httpServletRequest, @AuthenticationPrincipal UserDetails userDetails) {
    log.info("The operation updateUserAccess has been executed with request: {}", lockUserPutRequest);
    LockUserPutResponse lockUserPutResponse = userService.updateUserAccess(lockUserPutRequest);
    log.info("The operation updateUserAccess has been executed with response: {}", lockUserPutResponse);
    EventRequest eventRequest = switch (lockUserPutRequest.getOperation()) {
      case LOCK -> EventRequest.builder()
          .localDateTime(LocalDateTime.now())
          .path(httpServletRequest.getRequestURI())
          .subject(userDetails.getUsername())
          .action("LOCK_USER")
          .object(String.format("Lock user %s", lockUserPutRequest.getUser().toLowerCase())).build();

      case UNLOCK -> EventRequest.builder()
          .localDateTime(LocalDateTime.now())
          .path(httpServletRequest.getRequestURI())
          .subject(userDetails.getUsername())
          .action("UNLOCK_USER")
          .object(String.format("Unlock user %s", lockUserPutRequest.getUser().toLowerCase())).build();

    };
    eventService.registerEvent(eventRequest);
    return lockUserPutResponse;
  }
}

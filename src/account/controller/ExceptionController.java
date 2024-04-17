package account.controller;

import account.dto.constant.OperationEnum;
import account.dto.request.EventRequest;
import account.dto.request.LockUserPutRequest;
import account.exception.ApiError;
import account.exception.BadRequestException;
import account.exception.BruteForceException;
import account.exception.LoginFailedException;
import account.exception.NotFoundException;
import account.exception.ResponseWrapper;
import account.model.AuthUser;
import account.model.Role;
import account.repository.UserRepository;
import account.security.LoginAttemptService;
import account.service.EventService;
import account.service.UserService;
import account.utils.AuthorizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {

  private final EventService eventService;

  private final LoginAttemptService loginAttemptService;

  private final UserService userService;

  private final UserRepository userRepository;

  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected ApiError handleBadRequestException(BadRequestException badRequestException,
      HttpServletRequest request) {
    return ApiError.builder()
        .path(request.getRequestURI())
        .status(HttpStatus.BAD_REQUEST.value())
        .timestamp(System.currentTimeMillis())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(badRequestException.getMessage())
        .build();
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  protected ApiError handleNotFoundException(NotFoundException notFoundException,
      HttpServletRequest httpServletRequest) {
    return ApiError.builder()
        .path(httpServletRequest.getRequestURI())
        .status(HttpStatus.NOT_FOUND.value())
        .timestamp(System.currentTimeMillis())
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message(notFoundException.getMessage())
        .build();
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected ApiError handleMethodArgumentNotValidException(
      MethodArgumentNotValidException badRequestException, HttpServletRequest request) {
    if (!badRequestException.getAllErrors().isEmpty()) {
      ObjectError objectError = badRequestException.getAllErrors().get(0);
      return ApiError.builder()
          .path(request.getRequestURI())
          .status(HttpStatus.BAD_REQUEST.value())
          .timestamp(System.currentTimeMillis())
          .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
          .message(objectError.getDefaultMessage())
          .build();
    }

    return ApiError.builder()
        .path(request.getRequestURI())
        .status(HttpStatus.BAD_REQUEST.value())
        .timestamp(System.currentTimeMillis())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .build();
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected ApiError handleBadRequestException(ConstraintViolationException badRequestException,
      HttpServletRequest request) {
    return ApiError.builder()
        .path(request.getRequestURI())
        .status(HttpStatus.BAD_REQUEST.value())
        .timestamp(System.currentTimeMillis())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(badRequestException.getMessage())
        .build();
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  protected ApiError handleAccessException(AccessDeniedException accessDeniedException,
      HttpServletRequest httpServletRequest) {
    log.info("It has occurred an AccessDeniedException");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentPrincipalName = authentication.getName();
    EventRequest eventRequest = EventRequest.builder()
        .localDateTime(LocalDateTime.now())
        .path(httpServletRequest.getRequestURI())
        .subject(currentPrincipalName)
        .action("ACCESS_DENIED")
        .object(httpServletRequest.getRequestURI()).build();
    eventService.registerEvent(eventRequest);
    return ApiError.builder()
        .path(httpServletRequest.getRequestURI())
        .status(HttpStatus.FORBIDDEN.value())
        .timestamp(System.currentTimeMillis())
        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
        .message("Access Denied!")
        .build();
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  protected ApiError handleBadCredentialsException(BadCredentialsException authenticationException,
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    log.info("It has occurred an BadCredentialsException: {}.",
        authenticationException.getMessage());
    String authorization = httpServletRequest.getHeader("authorization");
    String subject;
    if (authorization != null) {
      subject = AuthorizationUtil.getUsernameFromAuthHeader(authorization);
      Optional<AuthUser> authUser = userRepository.findByEmail(subject);
      EventRequest eventRequest = EventRequest.builder()
          .localDateTime(LocalDateTime.now())
          .path(httpServletRequest.getRequestURI())
          .subject(subject)
          .action("LOGIN_FAILED")
          .object(httpServletRequest.getRequestURI()).build();
      eventService.registerEvent(eventRequest);
      if (loginAttemptService.isBlocked(subject) && authUser.isPresent() &&
          !userIsAdministrator(authUser.get())) {
        eventRequest = EventRequest.builder()
            .localDateTime(LocalDateTime.now())
            .path(httpServletRequest.getRequestURI())
            .subject(subject)
            .action("BRUTE_FORCE")
            .object(httpServletRequest.getRequestURI()).build();
        eventService.registerEvent(eventRequest);
        LockUserPutRequest lockUserPutRequest = LockUserPutRequest.builder()
            .user(subject)
            .operation(OperationEnum.LOCK).build();
        userService.updateUserAccess(lockUserPutRequest);
        eventRequest = EventRequest.builder()
            .localDateTime(LocalDateTime.now())
            .path(httpServletRequest.getRequestURI())
            .subject(subject)
            .action("LOCK_USER")
            .object(String.format("Lock user %s", lockUserPutRequest.getUser())).build();
        eventService.registerEvent(eventRequest);
      }
    }
    return ApiError.builder()
        .path(httpServletRequest.getRequestURI())
        .status(HttpStatus.UNAUTHORIZED.value())
        .timestamp(System.currentTimeMillis())
        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
        .message(authenticationException.getMessage())
        .build();
  }

//  @ExceptionHandler(LoginFailedException.class)
//  @ResponseStatus(HttpStatus.UNAUTHORIZED)
//  protected ApiError handleAuthenticationException(LoginFailedException authenticationException,
//      HttpServletRequest httpServletRequest) {
//    log.info("It has occurred an LoginFailedException: {}",
//        authenticationException.getMessage());
//    EventRequest eventRequest = EventRequest.builder()
//        .localDateTime(LocalDateTime.now())
//        .path(httpServletRequest.getRequestURI())
//        .subject(authenticationException.getUsername())
//        .action("LOGIN_FAILED")
//        .object(httpServletRequest.getRequestURI()).build();
//    eventService.registerEvent(eventRequest);
//    return ApiError.builder()
//        .path(httpServletRequest.getRequestURI())
//        .status(HttpStatus.UNAUTHORIZED.value())
//        .timestamp(System.currentTimeMillis())
//        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
//        .message("Login Failed!")
//        .build();
//  }
//
//  @ExceptionHandler(BruteForceException.class)
//  @ResponseStatus(HttpStatus.FORBIDDEN)
//  protected ApiError handleBruteForceException(BruteForceException bruteForceException,
//      HttpServletRequest httpServletRequest) {
//    log.info("It has occurred an BruteForceException");
//    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//    String currentPrincipalName = authentication.getName();
//    EventRequest eventRequest = EventRequest.builder()
//        .localDateTime(LocalDateTime.now())
//        .path(httpServletRequest.getRequestURI())
//        .subject(currentPrincipalName)
//        .action("BRUTE_FORCE")
//        .object(httpServletRequest.getRequestURI()).build();
//    eventService.registerEvent(eventRequest);
//    return ApiError.builder()
//        .path(httpServletRequest.getRequestURI())
//        .status(HttpStatus.FORBIDDEN.value())
//        .timestamp(System.currentTimeMillis())
//        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
//        .message("User Blocked!")
//        .build();
//  }

  @ExceptionHandler({InsufficientAuthenticationException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  protected ApiError handleAuthenticationException(
      AuthenticationException authenticationException,
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    log.info("It has occurred an AuthenticationException: {} {}",
        authenticationException.getMessage(), authenticationException.getClass().getName());
    String authorization = httpServletRequest.getHeader("authorization");
    String subject = null;
    if (authorization != null) {
      authorization = authorization.split(" ")[1];
      byte[] decodedBytes = Base64.getDecoder().decode(authorization);
      String decodedString = new String(decodedBytes);
      log.info("Authorization: {}", decodedString);
      subject = decodedString.split(":")[0];
      EventRequest eventRequest = EventRequest.builder()
          .localDateTime(LocalDateTime.now())
          .path(httpServletRequest.getRequestURI())
          .subject(subject)
          .action("LOGIN_FAILED")
          .object(httpServletRequest.getRequestURI()).build();
      eventService.registerEvent(eventRequest);
    }
    return ApiError.builder()
        .path(httpServletRequest.getRequestURI())
        .status(HttpStatus.UNAUTHORIZED.value())
        .timestamp(System.currentTimeMillis())
        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
        .message(authenticationException.getMessage())
        .build();
  }

  @ExceptionHandler({LockedException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  protected ApiError handleLockedException(
      AuthenticationException authenticationException,
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    log.info("It has occurred an AuthenticationException: {} {}",
        authenticationException.getMessage(), authenticationException.getClass().getName());

    return ApiError.builder()
        .path(httpServletRequest.getRequestURI())
        .status(HttpStatus.UNAUTHORIZED.value())
        .timestamp(System.currentTimeMillis())
        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
        .message(authenticationException.getMessage())
        .build();
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<Object> handleException(Exception ex) {
    ex.printStackTrace();
    log.info("The exception is: {}", ex.getMessage());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("message", ex.getMessage());
    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private boolean userIsAdministrator(AuthUser authUser) {
    return authUser.getRoles().stream().
        map(Role::getRole).anyMatch(role-> role.equals("ADMINISTRATOR"));
  }
}

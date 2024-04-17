package account.service;

import account.dto.constant.OperationEnum;
import account.dto.request.LockUserPutRequest;
import account.dto.request.RolePutRequest;
import account.dto.request.UserRequest;
import account.dto.response.LockUserPutResponse;
import account.dto.response.RolePutResponse;
import account.dto.response.UserDeleteResponse;
import account.dto.response.UserResponse;
import account.exception.BadRequestException;
import account.exception.NotFoundException;
import account.model.AuthUser;
import account.model.Role;
import account.repository.RoleRepository;
import account.repository.UserRepository;
import account.security.LoginAttemptService;
import account.security.PasswordValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final PasswordValidator passwordValidator;

  private final RoleRepository roleRepository;

  private final LoginAttemptService loginAttemptService;

  private static final String ROLE_ADMINISTRATOR = "ADMINISTRATOR";

  private static final String ROLE_USER = "USER";

  public UserResponse registerUser(UserRequest userRequest) {
    log.info("Starting the register of the user: {}", userRequest);
    boolean existsByEmail = userRepository.existsByEmail(userRequest.getEmail().toLowerCase());
    if (existsByEmail) {
      log.error("This email: {} is used by another user.", userRequest.getEmail());
      throw new BadRequestException("User exist!");
    }

    log.info("Validating the password.");
    passwordValidator.validPasswordWithBreachedPasswords(userRequest.getPassword());

    AuthUser user = AuthUser.builder()
        .name(userRequest.getName())
        .email(userRequest.getEmail().toLowerCase())
        .lastname(userRequest.getLastname())
        .password(passwordEncoder.encode(userRequest.getPassword())).build();

    long count = userRepository.count();
    Optional<Role> optionalRole;
    if (count == 0) {
      optionalRole = roleRepository.findByRole(ROLE_ADMINISTRATOR);
    } else {
      optionalRole = roleRepository.findByRole(ROLE_USER);
    }
    user.setRoles(List.of(optionalRole.get()));
    user.setUnlock(true);
    userRepository.save(user);
    Role roleToUpdate = optionalRole.get();
    List<AuthUser> authUsers = new ArrayList<>(roleToUpdate.getAuthUsers());
    authUsers.add(user);
    roleToUpdate.setAuthUsers(authUsers);
    roleRepository.save(roleToUpdate);

    List<String> roles = user.getRoles().stream()
        .map(role -> "ROLE_" + role.getRole()).sorted().toList();

    return UserResponse.builder()
        .id(user.getUserId())
        .name(user.getName())
        .lastname(user.getLastname())
        .email(user.getEmail())
        .roles(roles)
        .build();
  }

  public List<UserResponse> retrieveUsers() {
    log.info("Retrieving the list of users.");
    List<AuthUser> authUsers = userRepository.findAll();
    return authUsers.stream().map(authUser -> {
      List<String> roles = authUser.getRoles().stream()
          .map(role -> "ROLE_" + role.getRole()).sorted().toList();
      return UserResponse.builder()
          .id(authUser.getUserId())
          .name(authUser.getName())
          .lastname(authUser.getLastname())
          .email(authUser.getEmail())
          .roles(roles)
          .build();
    }).toList();
  }

  public RolePutResponse updateRolesInUser(RolePutRequest rolePutRequest) {
    log.info("Starting the process to update the roles of the user "
        + "with the following request: {}", rolePutRequest);
    String email = rolePutRequest.getUser();
    Optional<AuthUser> authUserOptional = userRepository.findByEmail(email.toLowerCase());
    AuthUser authUser = authUserOptional.orElseThrow(
        () -> {
          log.error("The user inside the request: {} has been not found.", rolePutRequest);
          return new NotFoundException("User not found!");
        });

    log.info("The user has been found : {}", authUser);
    log.info("The roles of the user are : {}", authUser.getRoles());
    Optional<Role> optionalRole = roleRepository.findByRole(rolePutRequest.getRole());
    if (optionalRole.isEmpty()) {
      log.error("The role inside the request: {} has been not found.", rolePutRequest);
      throw new NotFoundException("Role not found!");
    }
    Role roleToUpdate = optionalRole.get();
    log.info("The role found to update is : {}", roleToUpdate);
    switch (rolePutRequest.getOperation()) {
      case "GRANT":
        log.info("Setting a grant operation in the roles.");
        if (!authUser.getRoles().isEmpty()) {
          List<String> roles = authUser.getRoles().stream()
              .map(Role::getRole).sorted().toList();
          if (roles.contains(ROLE_ADMINISTRATOR) && !rolePutRequest.getRole()
              .equals(ROLE_ADMINISTRATOR)) {
            log.error("The user cannot combine administrative and business roles!");
            throw new BadRequestException(
                "The user cannot combine administrative and business roles!");
          }
          if (rolePutRequest.getRole().equals(ROLE_ADMINISTRATOR) && !roles.contains(
              ROLE_ADMINISTRATOR)) {
            log.error("The user cannot combine administrative and business roles!");
            throw new BadRequestException(
                "The user cannot combine administrative and business roles!");
          }

          authUser.getRoles().add(optionalRole.get());
          roleToUpdate.getAuthUsers().add(authUser);
        }
        break;
      case "REMOVE":
        log.info("Removing a grant operation in the roles.");
        if (rolePutRequest.getRole().equals(ROLE_ADMINISTRATOR)) {
          log.error("Can't remove ADMINISTRATOR role!");
          throw new BadRequestException("Can't remove ADMINISTRATOR role!");
        }

        if (!authUser.getRoles().isEmpty()) {
          Optional<Role> optionalRoleToRemove = authUser.getRoles().stream()
              .filter(role -> role.getRole().equals(rolePutRequest.getRole()))
              .findAny();
          if (optionalRoleToRemove.isEmpty()) {
            throw new BadRequestException("The user does not have a role!");
          }
          log.info("The role to remove is: {}", optionalRoleToRemove);
          authUser.getRoles().removeIf(
              role -> role.getRole().equals(optionalRoleToRemove.get().getRole()));
          roleToUpdate.getAuthUsers().removeIf(user -> user.getName().equals(authUser.getName()));

          log.info("The roles of the user are : {}", authUser.getRoles());
          if (authUser.getRoles().isEmpty()) {
            log.error("TThe user must have at least one role!");
            throw new BadRequestException("The user must have at least one role!");
          }
        }
        break;
    }

    log.info("Saving data the authUser: {}", authUser);
    log.info("Saving data the role to update: {}", roleToUpdate);
    userRepository.save(authUser);
    roleRepository.save(roleToUpdate);

    List<String> roles = null;
    if (!authUser.getRoles().isEmpty()) {
      log.info("Ordering the list of roles to retrieve the response.");

      roles = authUser.getRoles().stream()
          .map(role -> "ROLE_" + role.getRole()).sorted().toList();
    }

    log.info("Saving successfully the user with his roles.");
    return RolePutResponse.builder().id(authUser.getUserId())
        .name(authUser.getName())
        .lastname(authUser.getLastname())
        .email(authUser.getEmail())
        .roles(roles).build();
  }

  public UserDeleteResponse deleteUser(String email) {
    log.info("Removing the user with the email: {}", email);
    Optional<AuthUser> authUserOptional = userRepository.findByEmail(email);
    AuthUser authUser = authUserOptional.orElseThrow(
        () -> new NotFoundException("User not found!"));
    List<String> roles = authUser.getRoles().stream()
        .map(Role::getRole).sorted().toList();
    if (roles.contains(ROLE_ADMINISTRATOR)) {
      log.error("Can't remove ADMINISTRATOR role!");
      throw new BadRequestException("Can't remove ADMINISTRATOR role!");
    }

    List<Role> authUserRoles = authUser.getRoles();
    for (Role authUserRole : authUserRoles) {
      authUserRole.getAuthUsers()
          .removeIf(authUserToDelete -> authUserToDelete.getEmail().equals(email));
    }
    roleRepository.saveAll(authUserRoles);
    userRepository.delete(authUser);

    return UserDeleteResponse.builder()
        .user(email)
        .status("Deleted successfully!")
        .build();
  }

  public LockUserPutResponse updateUserAccess(LockUserPutRequest lockUserPutRequest){
    Optional<AuthUser> currentAuthUser = userRepository.findByEmail(lockUserPutRequest.getUser().toLowerCase());
    if(currentAuthUser.isEmpty()){
      throw new NotFoundException("User not found.");
    }
    AuthUser authUser = currentAuthUser.get();
    if(userIsAdministrator(authUser)){
      throw new BadRequestException("Can't lock the ADMINISTRATOR!");
    }
    authUser.setUnlock(lockUserPutRequest.getOperation().equals(OperationEnum.UNLOCK));
    userRepository.save(authUser);
    String userAccess = authUser.isUnlock() ? "unlocked" : "locked";
    String status = String.format("User %s %s!", authUser.getEmail().toLowerCase(), userAccess);
    log.info("The status of the lock was: {}", status);
    if(authUser.isUnlock()){
      loginAttemptService.unLockUser(lockUserPutRequest.getUser().toLowerCase());
    }
    return LockUserPutResponse.builder()
        .status(status).build();
  }

  private boolean userIsAdministrator(AuthUser authUser) {
    return authUser.getRoles().stream().
        map(Role::getRole).anyMatch(role-> role.equals("ADMINISTRATOR"));
  }
}

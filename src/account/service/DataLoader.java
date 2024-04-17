package account.service;

import account.model.Role;
import account.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

  private RoleRepository roleRepository;

  @Autowired
  public DataLoader(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
    createRoles();
  }

  private void createRoles() {
    try {

      if (roleRepository.count() == 0) {
        roleRepository.save(Role.builder().role("ADMINISTRATOR").build());
        roleRepository.save(Role.builder().role("USER").build());
        roleRepository.save(Role.builder().role("ACCOUNTANT").build());
        roleRepository.save(Role.builder().role("AUDITOR").build());
      }
    } catch (Exception e) {

    }
  }
}
package account.repository;

import account.model.AuthUser;
import account.model.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByRole(String role);

  @Transactional
  long deleteByAuthUsersIn(List<AuthUser> authUsers);
}

package account.repository;

import account.model.AuthUser;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AuthUser, Long> {

  boolean existsByEmail(String email);

  Optional<AuthUser> findByEmail(String email);

  long countByEmailIn(Set<String> emails);
}

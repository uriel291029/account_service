package account.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString.Exclude;

@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

  @Id
  @GeneratedValue
  private Long userId;

  private String name;

  private String lastname;

  private String email;

  private String password;

  @Exclude
  @ManyToMany(mappedBy = "authUsers", cascade = {CascadeType.DETACH, CascadeType.MERGE,
      CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
  private List<Role> roles;

  private boolean unlock;
}

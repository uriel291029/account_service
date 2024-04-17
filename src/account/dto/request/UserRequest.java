package account.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserRequest {

  @NotBlank
  private String name;

  @NotBlank
  private String lastname;

  @NotBlank
  @Email(regexp = ".+@acme\\.com$")
  private String email;

  @NotBlank
  @Size(min = 12, message = "Password length must be 12 chars minimum!")
  private String password;
}

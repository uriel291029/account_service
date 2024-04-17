package account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePutRequest {

  @NotBlank
  private String user;

  @NotBlank
  private String role;

  @Pattern(regexp = "GRANT|REMOVE")
  private String operation;

}

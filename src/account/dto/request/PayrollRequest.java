package account.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRequest {

  @NotBlank
  @Email(regexp = ".+@acme\\.com$")
  private String employee;

  @Pattern(regexp = "^(0[1-9]|1[0-2])-\\d{4}$")
  private String period;

  @Positive
  private Long salary;

}

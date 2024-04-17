package account.dto.request;

import account.dto.constant.OperationEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockUserPutRequest {

  @NotBlank
  private String user;

  private OperationEnum operation;
}

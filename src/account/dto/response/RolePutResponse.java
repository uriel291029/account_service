package account.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePutResponse {

   private Long id;

   private String name;

   private String lastname;

   private String email;

   private List<String> roles;
}

package account.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

  private Long id;

  private LocalDateTime date;

  private String action;

  private String subject;

  private String object;

  private String path;
}

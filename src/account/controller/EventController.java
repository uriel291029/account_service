package account.controller;

import account.dto.response.EventResponse;
import account.service.EventService;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api")
public class EventController {

  private final EventService eventService;

  @RolesAllowed({"ROLE_AUDITOR"})
  @GetMapping("security/events/")
  public List<EventResponse> retrieveEvents() {
    log.info("Consuming the service security/events");
    return eventService.retrieveEventResponses();
  }
}

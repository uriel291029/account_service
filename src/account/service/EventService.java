package account.service;

import account.dto.request.EventRequest;
import account.dto.response.EventResponse;
import account.model.Event;
import account.repository.EventRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;

  public void registerEvent(EventRequest eventRequest) {
    Event event = Event.builder()
        .path(eventRequest.getPath())
        .action(eventRequest.getAction())
        .subject(eventRequest.getSubject())
        .object(eventRequest.getObject())
        .localDateTime(eventRequest.getLocalDateTime())
        .build();
    eventRepository.save(event);
    log.info("The event {} has been saved successfully.", event);
  }

  public List<EventResponse> retrieveEventResponses() {
    return eventRepository.findAll(Sort.by(Direction.ASC, "eventId"))
        .stream().map(event ->
            EventResponse.builder().action(event.getAction())
                .id(event.getEventId())
                .object(event.getObject())
                .path(event.getPath())
                .subject(event.getSubject())
                .date(event.getLocalDateTime()).build()
        ).toList();
  }
}

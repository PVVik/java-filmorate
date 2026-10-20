package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventStorage eventStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("getFeed: должен вернуть DTO с epoch-секундами и строковыми enum")
    void getFeed_shouldMapEventsToDtos() {
        User user = User.builder().id(1L).build();
        given(userStorage.getUserById(1L)).willReturn(user);

        Instant ts = Instant.parse("2024-01-01T12:00:00Z");
        Event event = Event.builder()
                .eventId(100L)
                .userId(1L)
                .entityId(7L)
                .eventType(EventType.LIKE)
                .operation(Operation.REMOVE)
                .timestamp(ts)
                .build();
        given(eventStorage.getUserFeed(1L, EventService.DEFAULT_FEED_LIMIT))
                .willReturn(List.of(event));

        List<EventDto> feed = eventService.getFeed(1L);

        assertThat(feed).hasSize(1);
        EventDto dto = feed.getFirst();
        assertThat(dto.getEventId()).isEqualTo(100L);
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getEntityId()).isEqualTo(7L);
        assertThat(dto.getTimestamp()).isEqualTo(ts.getEpochSecond());
        assertThat(dto.getEventType()).isEqualTo("LIKE");
        assertThat(dto.getOperation()).isEqualTo("REMOVE");
    }

    @Test
    @DisplayName("getFeed: должен бросить NotFoundException, если пользователь не существует")
    void getFeed_shouldThrow_whenUserNotFound() {
        given(userStorage.getUserById(999L))
                .willThrow(new NotFoundException("Пользователя с id 999 не существует"));

        assertThatThrownBy(() -> eventService.getFeed(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не существует");
    }

    @Test
    @DisplayName("addLikeEvent: должен создать событие LIKE с указанной операцией")
    void addLikeEvent_shouldCreateLikeEvent() {
        eventService.addLikeEvent(1L, 2L, Operation.ADD);

        verify(eventStorage).create(argThat(e ->
                        e.getUserId() == 1L
                                && e.getEntityId() == 2L
                                && e.getEventType() == EventType.LIKE
                                && e.getOperation() == Operation.ADD
                // УБРАЛИ проверку timestamp != null — на этом этапе его ещё нет
        ));
    }


    @Test
    @DisplayName("addFriendEvent: должен создать событие FRIEND")
    void addFriendEvent_shouldCreateFriendEvent() {
        eventService.addFriendEvent(1L, 2L, Operation.REMOVE);

        verify(eventStorage).create(argThat(e ->
                e.getUserId() == 1L
                        && e.getEntityId() == 2L
                        && e.getEventType() == EventType.FRIEND
                        && e.getOperation() == Operation.REMOVE));
    }

    @Test
    @DisplayName("addReviewEvent: должен создать событие REVIEW с операцией UPDATE")
    void addReviewEvent_shouldCreateReviewEvent() {
        eventService.addReviewEvent(3L, 4L, Operation.UPDATE);

        verify(eventStorage).create(argThat(e ->
                e.getUserId() == 3L
                        && e.getEntityId() == 4L
                        && e.getEventType() == EventType.REVIEW
                        && e.getOperation() == Operation.UPDATE));
    }
}
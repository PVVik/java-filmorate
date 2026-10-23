package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.event.EventRowMapper;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({EventDbStorage.class, EventRowMapper.class})
class EventDbStorageTest {

    @Autowired
    private EventDbStorage eventStorage;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("create: должен сохранить событие и вернуть его с идентификатором")
    void create_shouldPersistEvent() {
        long userId = createTestUser("feed-create@test.com", "feedcreate");

        Event saved = eventStorage.create(Event.builder()
                .userId(userId)
                .entityId(10L)
                .eventType(EventType.LIKE)
                .operation(Operation.ADD)
                .build());

        assertThat(saved.getEventId()).isPositive();

        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE event_id = ?", Long.class, saved.getEventId());
        assertThat(count).isEqualTo(1L);

        Event fromDb = jdbc.queryForObject(
                "SELECT event_id, user_id, entity_id, event_type, operation, event_timestamp " +
                        "FROM events WHERE event_id = ?",
                new EventRowMapper(), saved.getEventId());
        assertThat(fromDb.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("getUserFeed: должен вернуть события только пользователя, свежие первыми, с лимитом")
    void getUserFeed_shouldFilterSortAndLimit() {
        long user1 = createTestUser("feed-user1@test.com", "feeduser1");
        long user2 = createTestUser("feed-user2@test.com", "feeduser2");

        eventStorage.create(Event.builder().userId(user1).entityId(1L).eventType(EventType.LIKE).operation(Operation.ADD).build());
        eventStorage.create(Event.builder().userId(user2).entityId(2L).eventType(EventType.FRIEND).operation(Operation.ADD).build());
        eventStorage.create(Event.builder().userId(user1).entityId(1L).eventType(EventType.LIKE).operation(Operation.REMOVE).build());

        List<Event> feed = eventStorage.getUserFeed(user1, 2);

        assertThat(feed).hasSize(2);
        assertThat(feed).allMatch(e -> e.getUserId() == user1);

        Instant first = feed.get(0).getTimestamp();
        Instant second = feed.get(1).getTimestamp();
        assertThat(first).isAfterOrEqualTo(second);
    }

    @Test
    @DisplayName("getUserFeed: должен корректно применять лимит при большом числе событий")
    void getUserFeed_shouldApplyLimit() {
        long userId = createTestUser("feed-limit@test.com", "feedlimit");

        for (int i = 0; i < 5; i++) {
            eventStorage.create(Event.builder()
                    .userId(userId)
                    .entityId(i)
                    .eventType(EventType.REVIEW)
                    .operation(Operation.ADD)
                    .build());
        }

        List<Event> feed = eventStorage.getUserFeed(userId, 3);

        assertThat(feed).hasSize(3);
        assertThat(feed.stream().map(Event::getTimestamp).allMatch(t -> t != null));
        for (int i = 0; i < feed.size() - 1; i++) {
            assertThat(feed.get(i).getTimestamp())
                    .isAfterOrEqualTo(feed.get(i + 1).getTimestamp());
        }
    }

    private Event event(long userId, EventType type, Operation operation, long entityId) {
        return Event.builder()
                .userId(userId)
                .entityId(entityId)
                .eventType(type)
                .operation(operation)
                .build();
    }

    private long createTestUser(String email, String login) {
        jdbc.update("INSERT INTO users (email, login, user_name, birthday) VALUES (?, ?, ?, ?)",
                email, login, login, java.time.LocalDate.of(1990, 1, 1));
        return jdbc.queryForObject("SELECT MAX(user_id) FROM users", Long.class);
    }
}

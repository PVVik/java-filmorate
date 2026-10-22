package ru.yandex.practicum.filmorate.storage.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.time.Instant;
import java.util.List;

@Repository
@Slf4j
public class EventDbStorage extends BaseRepository<Event> implements EventStorage {

    public EventDbStorage(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Event create(Event event) {
        String query = "INSERT INTO events (user_id, entity_id, event_type, operation) " +
                "VALUES (?, ?, ?, ?)";

        long id = insert(query,
                event.getUserId(),
                event.getEntityId(),
                event.getEventType().name(),
                event.getOperation().name());

        event.setEventId(id);

        String selectQuery = "SELECT event_timestamp FROM events WHERE event_id = ?";
        Instant timestamp = jdbc.queryForObject(selectQuery, (rs, rowNum) ->
                rs.getTimestamp("event_timestamp").toInstant(), id);

        event.setTimestamp(timestamp);

        log.info("Создали событие с id {} типа {} и операцией {} для пользователя {}",
                id, event.getEventType(), event.getOperation(), event.getUserId());

        return event;
    }

    @Override
    public List<Event> getUserFeed(long userId, int limit) {
        String query = """
                    SELECT e.event_id, e.user_id, e.entity_id, e.event_type, e.operation, e.event_timestamp
                    FROM events e
                    WHERE e.user_id = ?
                       OR e.user_id IN (
                           SELECT f.friend_id
                           FROM friendship f
                           WHERE f.user_id = ?
                           UNION ALL
                           SELECT f.user_id
                           FROM friendship f
                           WHERE f.friend_id = ?
                       )
                    ORDER BY e.event_timestamp ASC, e.event_id ASC
                    LIMIT ?
                """;

        log.info("Получаем ленту пользователя {} с лимитом {}", userId, limit);
        return findMany(query, userId, userId, userId, limit);
    }


}

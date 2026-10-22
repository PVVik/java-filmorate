package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class EventService {

    public static final int DEFAULT_FEED_LIMIT = 20;

    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public EventService(EventStorage eventStorage,
                        @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.eventStorage = eventStorage;
        this.userStorage = userStorage;
    }

    public List<EventDto> getFeed(long userId) {
        userStorage.getUserById(userId);

        List<EventDto> feed = eventStorage.getUserFeed(userId, DEFAULT_FEED_LIMIT).stream()
                .map(EventMapper::mapToDto)
                .toList();

        log.info("Получили ленту событий пользователя {} (событий: {})", userId, feed.size());

        return feed;
    }

    public void addLikeEvent(long userId, long entityId, Operation operation) {
        createEvent(userId, entityId, EventType.LIKE, operation);
    }

    public void addFriendEvent(long userId, long entityId, Operation operation) {
        createEvent(userId, entityId, EventType.FRIEND, operation);
    }

    public void addReviewEvent(long userId, long entityId, Operation operation) {
        createEvent(userId, entityId, EventType.REVIEW, operation);
    }

    private void createEvent(long userId, long entityId, EventType eventType, Operation operation) {
        eventStorage.create(Event.builder()
                .userId(userId)
                .entityId(entityId)
                .eventType(eventType)
                .operation(operation)
                .build());

        log.debug("Создано событие: type={}, operation={}, userId={}, entityId={}",
                eventType, operation, userId, entityId);
    }
}

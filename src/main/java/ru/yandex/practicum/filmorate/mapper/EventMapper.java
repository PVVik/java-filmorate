package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

public class EventMapper {

    private EventMapper() {
    }

    public static EventDto mapToDto(Event event) {
        return EventDto.builder()
                .timestamp(event.getTimestamp().toEpochMilli())
                .userId(event.getUserId())
                .eventType(event.getEventType().name())
                .operation(event.getOperation().name())
                .eventId(event.getEventId())
                .entityId(event.getEntityId())
                .build();
    }
}

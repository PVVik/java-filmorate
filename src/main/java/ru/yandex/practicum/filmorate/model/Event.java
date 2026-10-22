package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(of = {"eventId"})
@ToString
@Builder
public class Event {

    private long eventId;
    private long userId;
    private long entityId;
    private EventType eventType;
    private Operation operation;
    private Instant timestamp;
}
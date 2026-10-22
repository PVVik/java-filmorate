package ru.yandex.practicum.filmorate.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {

    private long timestamp;
    private long userId;
    private String eventType;
    private String operation;
    private long eventId;
    private long entityId;
}
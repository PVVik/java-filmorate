package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {

    Event create(Event event);

    List<Event> getUserFeed(long userId, int limit);
}
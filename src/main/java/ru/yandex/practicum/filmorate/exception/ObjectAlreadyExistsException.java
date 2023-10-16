package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectAlreadyExistsException extends RuntimeException {

    public ObjectAlreadyExistsException(final String message) {
        super(message);
        log.error(message);
    }
}
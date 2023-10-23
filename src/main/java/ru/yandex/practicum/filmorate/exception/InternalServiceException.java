package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternalServiceException extends RuntimeException {

    public InternalServiceException(final String message) {
        super(message);
        log.error(message);
    }
}

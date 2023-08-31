package ru.yandex.practicum.filmorate.exceptions;

public class InternalServiceException extends RuntimeException {

    public InternalServiceException(final String message) {
        super(message);
    }
}

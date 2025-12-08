package ru.yandex.javacourse.schedule.exceptions;

public class InvalidEpicIdException extends RuntimeException {
    public InvalidEpicIdException(String message) {
        super(message);
    }
}

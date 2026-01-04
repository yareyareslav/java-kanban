package ru.yandex.javacourse.schedule.exceptions;

public class TimeIntersectionException extends RuntimeException {
    public TimeIntersectionException(String message) {
        super(message);
    }
}

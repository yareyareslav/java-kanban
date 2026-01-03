package ru.yandex.javacourse.schedule.exceptions;

public class TaskIntersectionException extends RuntimeException {
    public TaskIntersectionException(String message) {
        super(message);
    }
}

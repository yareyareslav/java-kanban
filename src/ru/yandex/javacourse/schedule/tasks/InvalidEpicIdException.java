package ru.yandex.javacourse.schedule.tasks;

public class InvalidEpicIdException extends RuntimeException {
    public InvalidEpicIdException(String message) {
        super(message);
    }
}

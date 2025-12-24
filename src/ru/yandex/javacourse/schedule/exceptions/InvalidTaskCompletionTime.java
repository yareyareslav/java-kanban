package ru.yandex.javacourse.schedule.exceptions;

public class InvalidTaskCompletionTime extends RuntimeException {
    public InvalidTaskCompletionTime(String message) {
        super(message);
    }
}

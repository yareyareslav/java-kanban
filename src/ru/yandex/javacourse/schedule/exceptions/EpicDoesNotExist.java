package ru.yandex.javacourse.schedule.exceptions;

public class EpicDoesNotExist extends RuntimeException {
    public EpicDoesNotExist(String message) {
        super(message);
    }
}

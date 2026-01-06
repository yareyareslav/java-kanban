package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.exceptions.TimeIntersectionException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends AbstractHandler {
    private enum Endpoint { GET_EPICS, GET_EPIC, GET_SUBTASKS, POST_EPIC, DELETE_EPIC, UNKNOWN }

    public EpicsHandler(TaskManager manager) {
        super(manager);
    }

    private Endpoint getEndpoint(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        return switch (method) {
            case "GET" -> {
                if (pathParts.length == 3) {
                    yield Endpoint.GET_EPIC;
                }
                if (pathParts.length == 4) {
                    yield Endpoint.GET_SUBTASKS;
                }
                yield Endpoint.GET_SUBTASKS;
            }
            case "POST" -> Endpoint.POST_EPIC;
            case "DELETE" -> Endpoint.DELETE_EPIC;
            default -> Endpoint.UNKNOWN;
        };
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(exchange);

            switch (endpoint) {
                case Endpoint.GET_EPICS -> handleGetEpics(exchange);
                case Endpoint.GET_EPIC -> handleGetEpicById(exchange);
                case Endpoint.GET_SUBTASKS -> handleGetSubtasks(exchange);
                case Endpoint.POST_EPIC -> handlePostEpic(exchange);
                case Endpoint.DELETE_EPIC -> handleDeleteEpic(exchange);
                default -> writeResponse(exchange, "Такого эндпоинта не существует", 404);
            }
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Ошибка синтаксиса JSON: " + e.getMessage(), 400);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        writeResponse(exchange, gson.toJson(manager.getEpics()), 200);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getId(exchange);

        if (epicIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор эпика", 400);
            return;
        }
        Optional<Epic> epicOpt = manager.getEpic(epicIdOpt.get());
        if (epicOpt.isEmpty()) {
            writeResponse(exchange, "Эпика с таким идентификатором не существует", 404);
            return;
        }
        writeResponse(exchange, gson.toJson(epicOpt.get()), 200);
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getId(exchange);

        if (epicIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор эпика", 400);
            return;
        }
        Optional<Epic> epicOpt = manager.getEpic(epicIdOpt.get());
        if (epicOpt.isEmpty()) {
            writeResponse(exchange, "Эпика с таким идентификатором не существует", 404);
            return;
        }
        List<Subtask> subtasks = epicOpt.get().getSubtaskIds()
                .stream()
                .map(manager::getSubtask)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        writeResponse(exchange, gson.toJson(subtasks), 200);
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

            if (body.isEmpty()) {
                writeResponse(exchange, "Тело запроса пусто", 400);
                return;
            }

            Epic epic = gson.fromJson(body, Epic.class);

            if (epic == null || epic.getName() == null || epic.getDescription() == null) {
                writeResponse(exchange, "Некорректный JSON или отсутствуют обязательные поля", 400);
                return;
            }

            manager.addNewEpic(epic);
            writeResponse(exchange, "Эпик создан", 201);
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Ошибка синтаксиса JSON: " + e.getMessage(), 400);
        }
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getId(exchange);

        if (epicIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 400);
            return;
        }

        int epicId = epicIdOpt.get();
        try {
            manager.deleteEpic(epicId);
            writeResponse(exchange, "Задача удалена", 200);
        } catch (NotFoundException e) {
            writeResponse(exchange, e.getMessage(), 404);
        }
    }
}

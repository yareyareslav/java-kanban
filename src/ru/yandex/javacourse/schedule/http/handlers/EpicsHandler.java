package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
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
                yield Endpoint.GET_EPICS;
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
            sendBadRequest(exchange, "Ошибка синтаксиса JSON: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendServer(exchange);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getEpics()));
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        try {
            Epic epic = manager.getEpic(getId(exchange));
            sendText(exchange, gson.toJson(epic));
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Некорректный идентификатор эпика");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        try {
            Epic epic = manager.getEpic(getId(exchange));
            List<Subtask> subtasks = epic.getSubtaskIds()
                    .stream()
                    .map(manager::getSubtask)
                    .toList();
            sendText(exchange, gson.toJson(subtasks));
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Некорректный идентификатор эпика");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

            if (body.isEmpty()) {
                sendBadRequest(exchange, "Тело запроса пусто");
                return;
            }

            Epic epicFromJson = gson.fromJson(body, Epic.class);
            Epic epic = new Epic(epicFromJson.getName(), epicFromJson.getDescription());

            if (epic.getName() == null || epic.getDescription() == null) {
                sendBadRequest(exchange, "Некорректный JSON или отсутствуют обязательные поля");
                return;
            }

            manager.addNewEpic(epic);
            sendUpdated(exchange, "Эпик создан");
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Ошибка синтаксиса JSON: " + e.getMessage());
        }
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        try {
            manager.deleteEpic(getId(exchange));
            sendText(exchange, "Задача удалена");
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Некорректный идентификатора эпика");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }
}

package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.exceptions.TimeIntersectionException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class SubtasksHandler extends AbstractHandler {
    private enum Endpoint {GET_SUBTASKS, GET_SUBTASK, POST_SUBTASK, DELETE_SUBTASK, UNKNOWN }

    public SubtasksHandler(TaskManager manager) {
        super(manager);
    }

    private Endpoint getEndpoint(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        return switch (method) {
            case "GET" -> pathParts.length == 3 ?
                    Endpoint.GET_SUBTASK :
                    Endpoint.GET_SUBTASKS;
            case "POST" -> Endpoint.POST_SUBTASK;
            case "DELETE" -> Endpoint.DELETE_SUBTASK;
            default -> Endpoint.UNKNOWN;
        };
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(exchange);

            switch (endpoint) {
                case Endpoint.GET_SUBTASKS -> handleGetTasks(exchange);
                case Endpoint.GET_SUBTASK -> handleGetTaskById(exchange);
                case Endpoint.POST_SUBTASK -> handlePostSubtask(exchange);
                case Endpoint.DELETE_SUBTASK -> handleDeleteSubtask(exchange);
                default -> writeResponse(exchange, "Такого эндпоинта не существует", 404);
            }
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Ошибка синтаксиса JSON: " + e.getMessage(), 400);
        }

    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        writeResponse(exchange, gson.toJson(manager.getTasks()), 200);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> subtaskOptId = getId(exchange);

        if (subtaskOptId.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор подзадачи", 400);
            return;
        }
        Optional<Subtask> subtaskOpt = manager.getSubtask(subtaskOptId.get());
        if (subtaskOpt.isEmpty()) {
            writeResponse(exchange, "Подзадачи с таким идентификатором не существует", 404);
            return;
        }
        writeResponse(exchange, gson.toJson(subtaskOpt.get()), 200);
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

            if (body.isEmpty()) {
                writeResponse(exchange, "Тело запроса пусто", 400);
                return;
            }

            Subtask subtask = gson.fromJson(body, Subtask.class);

            if (subtask == null || subtask.getName() == null || subtask.getDescription() == null || subtask.getStatus() == null) {
                writeResponse(exchange, "Некорректный JSON или отсутствуют обязательные поля", 400);
                return;
            }

            try {
                String response;
                if (subtask.getId() == 0) {
                    response = "Подзадача создана";
                    manager.addNewSubtask(subtask);
                } else {
                    response = "Подзадача обновлена";
                    manager.updateSubtask(subtask);
                }
                writeResponse(exchange, response, 201);
            } catch (TimeIntersectionException e) {
                writeResponse(exchange, e.getMessage(), 406);
            } catch (NotFoundException e) {
                writeResponse(exchange, e.getMessage(), 404);
            }
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Ошибка синтаксиса JSON: " + e.getMessage(), 400);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        Optional<Integer> subtaskOptId = getId(exchange);

        if (subtaskOptId.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор подзадачи", 400);
            return;
        }

        try {
            manager.deleteSubtask(subtaskOptId.get());
            writeResponse(exchange, "Подзадача удалена", 200);
        } catch (NotFoundException e) {
            writeResponse(exchange, e.getMessage(), 404);
        }
    }
}

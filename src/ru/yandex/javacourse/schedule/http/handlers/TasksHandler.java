package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.exceptions.TimeIntersectionException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.io.InputStream;

public class TasksHandler extends BaseHttpHandler {
    private enum Endpoint { GET_TASKS, GET_TASK, POST_TASK, DELETE_TASK, UNKNOWN }

    public TasksHandler(TaskManager manager) {
        super(manager);
    }

    private Endpoint getEndpoint(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        return switch (method) {
            case "GET" -> pathParts.length == 3 ?
                    Endpoint.GET_TASK :
                    Endpoint.GET_TASKS;
            case "POST" -> Endpoint.POST_TASK;
            case "DELETE" -> Endpoint.DELETE_TASK;
            default -> Endpoint.UNKNOWN;
        };
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(exchange);

            switch (endpoint) {
                case Endpoint.GET_TASKS -> handleGetTasks(exchange);
                case Endpoint.GET_TASK -> handleGetTaskById(exchange);
                case Endpoint.POST_TASK -> handlePostTask(exchange);
                case Endpoint.DELETE_TASK -> handleDeleteTask(exchange);
                default -> writeResponse(exchange, "Такого эндпоинта не существует", 404);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Ошибка синтаксиса JSON: " + e.getMessage());
        } catch (Exception e) {
            sendServer(exchange);
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getTasks()));
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
            try {
                int taskId = getId(exchange);
                Task task = manager.getTask(taskId);
                sendText(exchange, gson.toJson(task));
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный идентификатор задачи");
            }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

            if (body.isEmpty()) {
                sendBadRequest(exchange, "Тело запроса пусто");
                return;
            }

            Task task = gson.fromJson(body, Task.class);

            if (task == null || task.getName() == null || task.getDescription() == null || task.getStatus() == null) {
                sendBadRequest(exchange, "Некорректный JSON или отсутствуют обязательные поля");
                return;
            }

            try {
                String response;
                if (task.getId() == 0) {
                    response = "Задача создана";
                    manager.addNewTask(task);
                } else {
                    response = "Задача обновлена";
                    manager.updateTask(task);
                }
                sendUpdated(exchange, response);
            } catch (TimeIntersectionException e) {
                sendHasIntersections(exchange, e.getMessage());
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        try {
            int taskId = getId(exchange);
            manager.deleteTask(taskId);
            sendText(exchange, "Задача удалена");
        } catch (NotFoundException e) {
            sendBadRequest(exchange, e.getMessage());
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Некорректный идентификатор задачи");
        }
    }
}

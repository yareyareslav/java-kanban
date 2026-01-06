package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.exceptions.TimeIntersectionException;
import ru.yandex.javacourse.schedule.http.adapters.DurationAdapter;
import ru.yandex.javacourse.schedule.http.adapters.LocalDateTimeAdapter;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class TasksHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private enum Endpoint { GET_TASKS, GET_TASK, POST_TASK, DELETE_TASK, UNKNOWN }
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    private Optional<Integer> getTaskId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Endpoint getEndpoint(HttpExchange exchange) throws IOException {
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

    private void writeResponse(HttpExchange exchange,
                               String responseString,
                               int responseCode) throws IOException {
        byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(responseCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
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
            writeResponse(exchange, "Ошибка синтаксиса JSON: " + e.getMessage(), 400);
        }

    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        writeResponse(exchange, gson.toJson(manager.getTasks()), 200);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
            Optional<Integer> taskIdOpt = getTaskId(exchange);

            if (taskIdOpt.isEmpty()) {
                writeResponse(exchange, "Некорректный идентификатор задачи", 400);
                return;
            }
            Optional<Task> taskOpt = manager.getTask(taskIdOpt.get());
            if (taskOpt.isEmpty()) {
                writeResponse(exchange, "Задачи с таким идентификатором не существует", 404);
                return;
            }
            writeResponse(exchange, gson.toJson(taskOpt.get()), 200);
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

            if (body.isEmpty()) {
                writeResponse(exchange, "Тело запроса пусто", 400);
                return;
            }

            Task task = gson.fromJson(body, Task.class);

            if (task == null || task.getName() == null || task.getDescription() == null || task.getStatus() == null) {
                writeResponse(exchange, "Некорректный JSON или отсутствуют обязательные поля", 400);
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

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);

        if (taskIdOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 400);
            return;
        }

        int taskId = taskIdOpt.get();
        if (manager.getTask(taskId).isEmpty()) {
            writeResponse(exchange, "Задачи с таким идентификатором не существует", 404);
            return;
        }

        manager.deleteTask(taskId);
        writeResponse(exchange, "Задача удалена", 200);
    }
}

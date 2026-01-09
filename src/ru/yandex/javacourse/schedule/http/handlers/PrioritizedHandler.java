package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    private enum Endpoint { GET_PRIORITIZED, UNKNOWN }

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    private Endpoint getEndpoint(HttpExchange exchange) {
        String method = exchange.getRequestMethod();

        return switch (method) {
            case "GET" -> Endpoint.GET_PRIORITIZED;
            default -> Endpoint.UNKNOWN;
        };
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(exchange);

            switch (endpoint) {
                case Endpoint.GET_PRIORITIZED -> handleGetPrioritized(exchange);
                default -> writeResponse(exchange, "Такого эндпоинта не существует", 404);
            }
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Ошибка синтаксиса JSON: " + e.getMessage(), 400);
        } catch (Exception e) {
            writeResponse(exchange, "Ошибка сервера", 500);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        writeResponse(exchange, gson.toJson(manager.getPrioritizedTasks()), 200);
    }
}

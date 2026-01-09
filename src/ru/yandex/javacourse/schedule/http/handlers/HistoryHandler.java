package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private enum Endpoint { GET_HISTORY, UNKNOWN }

    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    private Endpoint getEndpoint(HttpExchange exchange) {
        String method = exchange.getRequestMethod();

        return switch (method) {
            case "GET" -> Endpoint.GET_HISTORY;
            default -> Endpoint.UNKNOWN;
        };
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Endpoint endpoint = getEndpoint(exchange);

            switch (endpoint) {
                case Endpoint.GET_HISTORY -> handleGetHistory(exchange);
                default -> sendNotFound(exchange, "Такого эндпоинта не существует");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Ошибка синтаксиса JSON: " + e.getMessage());
        } catch (Exception e) {
            sendServer(exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getHistory()));
    }
}

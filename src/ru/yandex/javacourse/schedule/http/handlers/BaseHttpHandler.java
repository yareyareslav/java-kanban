package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.http.adapters.DurationAdapter;
import ru.yandex.javacourse.schedule.http.adapters.LocalDateTimeAdapter;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    protected final TaskManager manager;

    public BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    protected void writeResponse(HttpExchange exchange,
                               String responseString,
                               int responseCode) throws IOException {
        byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(responseCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendText(HttpExchange exchange, String response) throws IOException {
        writeResponse(exchange, response, 200);
    }

    protected void sendUpdated(HttpExchange exchange, String response) throws IOException {
        writeResponse(exchange, response, 201);
    }

    protected void sendBadRequest(HttpExchange exchange, String response) throws IOException {
        writeResponse(exchange, response, 400);
    }

    protected void sendNotFound(HttpExchange exchange, String response) throws IOException {
        writeResponse(exchange, response, 404);
    }

    protected void sendHasIntersections(HttpExchange exchange, String response) throws IOException {
        writeResponse(exchange, response, 406);
    }

    protected void sendServer(HttpExchange exchange) throws IOException {
        writeResponse(exchange, "Ошибка сервера", 500);
    }

    protected Integer getId(HttpExchange exchange) throws NumberFormatException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        return Integer.parseInt(pathParts[2]);

    }
}

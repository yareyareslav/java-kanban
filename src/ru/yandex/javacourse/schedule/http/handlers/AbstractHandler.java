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
import java.util.Optional;

public abstract class AbstractHandler implements HttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    protected final TaskManager manager;

    public AbstractHandler(TaskManager manager) {
        this.manager = manager;
    }

    protected void writeResponse(HttpExchange exchange,
                               String responseString,
                               int responseCode) throws IOException {
        byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(responseCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected Optional<Integer> getId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}

package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.javacourse.schedule.http.HttpTaskServer;
import ru.yandex.javacourse.schedule.http.adapters.DurationAdapter;
import ru.yandex.javacourse.schedule.http.adapters.LocalDateTimeAdapter;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpHandlerTest {
    protected final TaskManager manager = Managers.getDefault();
    protected final HttpTaskServer server = new HttpTaskServer(manager);
    protected final HttpClient client = HttpClient.newHttpClient();
    protected final String ORIGIN = "http://localhost:";
    protected final int PORT = 8081;
    private final String URL;

    protected final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public BaseHttpHandlerTest(final String ROUTE) {
        this.URL = ORIGIN + PORT + ROUTE;
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        server.start();
    }

    @AfterEach
    public void shutDown() {
        server.close(0);
    }

    protected HttpRequest buildGetRequest(final String POSTFIX) {
        return HttpRequest
                .newBuilder()
                .uri(URI.create(URL + POSTFIX))
                .GET()
                .build();
    }

    protected HttpRequest buildPostRequest(final String BODY) {
        return HttpRequest
                .newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(BODY))
                .build();
    }

    protected HttpRequest buildDeleteRequest(final String POSTFIX) {
        return HttpRequest
                .newBuilder()
                .uri(URI.create(URL + POSTFIX))
                .DELETE()
                .build();
    }
}

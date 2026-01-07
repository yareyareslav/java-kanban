package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.adapters.DurationAdapter;
import ru.yandex.javacourse.schedule.http.adapters.LocalDateTimeAdapter;
import ru.yandex.javacourse.schedule.http.tokens.SubtaskListTypeToken;
import ru.yandex.javacourse.schedule.http.tokens.TaskListTypeToken;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskServerTest {
    private final TaskManager manager = Managers.getDefault();
    private final HttpTaskServer server = new HttpTaskServer(manager);
    private final HttpClient client = HttpClient.newHttpClient();
    private final String ORIGIN = "http://localhost:";
    private final int PORT = 8081;

    protected final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public HttpTaskServerTest() throws IOException {}

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

    @Test
    public void getTasks_shouldReturnTasksFromManager() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addNewTask(task);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.size(), "Некорректное количество задач");
        assertEquals("Test", tasks.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void getTask_shouldReturnTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addNewTask(task);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks/1"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task taskFromServer = gson.fromJson(response.body(), Task.class);

        assertEquals(task.getId(), taskFromServer.getId(), "Id задачи должен совпадать");
        assertEquals(task.getName(), taskFromServer.getName(), "Имя задачи должно совпадать");
        assertEquals(task.getDescription(), taskFromServer.getDescription(), "Описание задачи должно совпадать");
        assertEquals(task.getStatus(), taskFromServer.getStatus(), "Статус задачи должно совпадать");
        assertEquals(task.getStartTime(), taskFromServer.getStartTime(), "Старт задачи должно совпадать");
        assertEquals(task.getDuration(), taskFromServer.getDuration(), "Срок задачи должно совпадать");
    }

    @Test
    public void getTask_shouldReturn404_noSuchId() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addNewTask(task);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks/2"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void getTask_shouldReturn400_badId() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addNewTask(task);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks/2dsfs"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    public void postTask_shouldCreateNewTask_noIdProvided() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getTasks().size(), "Должна быть 1 задача");
        assertEquals(task.getName(), manager.getTask(1).getName());
        assertEquals(task.getDescription(), manager.getTask(1).getDescription());
        assertEquals(task.getStartTime(), manager.getTask(1).getStartTime());
        assertEquals(task.getDuration(), manager.getTask(1).getDuration());
    }

    @Test
    public void postTask_shouldUpdateNewTask_idProvided() throws IOException, InterruptedException {
        Task task1 = new Task(1, "Test 1", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Task task2 = new Task(1, "Test 2", "Testing Updated task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusYears(1));
        manager.addNewTask(task1);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getTasks().size(), "Должна быть 1 задача");
        assertEquals(task2.getName(), manager.getTask(1).getName());
        assertEquals(task2.getDescription(), manager.getTask(1).getDescription());
        assertEquals(task2.getStartTime(), manager.getTask(1).getStartTime());
        assertEquals(task2.getDuration(), manager.getTask(1).getDuration());
    }

    @Test
    public void postTask_shouldReturn404_wrongIdProvidedForUpdate() throws IOException, InterruptedException {
        Task task1 = new Task(1, "Test 1", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Task task2 = new Task(2, "Test 2", "Testing Updated task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusYears(1));
        manager.addNewTask(task1);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void postTask_shouldReturn406_timeIntersects() throws IOException, InterruptedException {
        Task task1 = new Task(1, "Test 1", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Task task2 = new Task(2, "Test 2", "Testing Updated task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addNewTask(task1);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void deleteTask_shouldDeleteTask() throws IOException, InterruptedException {
        Task task = new Task(1, "Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addNewTask(task);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks/1"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    public void getSubtasks_shouldReturnSubtasksFromManager() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(1, "Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertNotNull(subtasks, "Задачи не возвращаются");
        assertEquals(1, subtasks.size(), "Некорректное количество задач");
        assertEquals("Test", subtasks.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void getSubtask_shouldReturnSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(1, "Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/subtasks/1"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task subtaskFromServer = gson.fromJson(response.body(), Subtask.class);

        assertEquals(subtask.getId(), subtaskFromServer.getId(), "Id подзадачи должен совпадать");
        assertEquals(subtask.getName(), subtaskFromServer.getName(), "Имя подзадачи должно совпадать");
        assertEquals(subtask.getDescription(), subtaskFromServer.getDescription(), "Описание подзадачи должно совпадать");
        assertEquals(subtask.getStatus(), subtaskFromServer.getStatus(), "Статус подзадачи должно совпадать");
        assertEquals(subtask.getStartTime(), subtaskFromServer.getStartTime(), "Старт подзадачи должно совпадать");
        assertEquals(subtask.getDuration(), subtaskFromServer.getDuration(), "Срок подзадачи должен совпадать");
    }

    @Test
    public void getSubtask_shouldReturn404_noSuchId() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(1, "Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/subtasks/4"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void getSubtask_shouldReturn400_badId() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(1, "Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/tasks/2dsfs"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    public void postSubtask_shouldCreateNewSubtask_noIdProvided() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getSubtasks().size(), "Должна быть 1 задача");
        assertEquals(subtask.getName(), manager.getSubtask(1).getName());
        assertEquals(subtask.getDescription(), manager.getSubtask(1).getDescription());
        assertEquals(subtask.getStartTime(), manager.getSubtask(1).getStartTime());
        assertEquals(subtask.getDuration(), manager.getSubtask(1).getDuration());
    }

    @Test
    public void postSubtask_shouldUpdateNewSubtask_idProvided() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask(1, "Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Subtask subtask2 = new Subtask(1, "Updated Test", "Updated Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask2)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getSubtasks().size(), "Должна быть 1 задача");
        assertEquals(subtask2.getName(), manager.getSubtask(1).getName());
        assertEquals(subtask2.getDescription(), manager.getSubtask(1).getDescription());
        assertEquals(subtask2.getStartTime(), manager.getSubtask(1).getStartTime());
        assertEquals(subtask2.getDuration(), manager.getSubtask(1).getDuration());
    }

    @Test
    public void postSubtask_shouldReturn404_wrongIdProvidedForUpdate() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask(1, "Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Subtask subtask2 = new Subtask(3, "Updated Test", "Updated Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now().plusYears(1));
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask2)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void postSubtask_shouldReturn406_timeIntersects() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask(1, "Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Updated Test", "Updated Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask2)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void deleteSubtask_shouldDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(1, "Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(ORIGIN + PORT + "/subtasks/1"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }
}

package ru.yandex.javacourse.schedule.http.handlers;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.tokens.TaskListTypeToken;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TasksHandlerTest extends BaseHttpHandlerTest {
    public TasksHandlerTest() {
        super("/tasks");
    }

    @Test
    public void getTasks_shouldReturnTasksFromManager() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addNewTask(task);

        HttpRequest request = buildGetRequest("");
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

        HttpRequest request = buildGetRequest("/1");
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

        HttpRequest request = buildGetRequest("/2");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void getTask_shouldReturn400_badId() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addNewTask(task);

        HttpRequest request = buildGetRequest("/2andString");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    public void postTask_shouldCreateNewTask_noIdProvided() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());

        HttpRequest request = buildPostRequest(gson.toJson(task));
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

        HttpRequest request = buildPostRequest(gson.toJson(task2));
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

        HttpRequest request = buildPostRequest(gson.toJson(task2));
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

        HttpRequest request = buildPostRequest(gson.toJson(task2));
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void deleteTask_shouldDeleteTask() throws IOException, InterruptedException {
        Task task = new Task(1, "Test", "Testing task",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addNewTask(task);

        HttpRequest request = buildDeleteRequest("/1");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task taskFromServer = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), taskFromServer.getId(), "Id должны быть равны");
        assertEquals(task.getName(), taskFromServer.getName(), "Имена должны быть одинаковыми");
        assertEquals(task.getDescription(), taskFromServer.getDescription(), "Описания должны быть одинаковыми");
        assertEquals(task.getDuration(), taskFromServer.getDuration(), "Продолжительности должны быть одинаковыми");
        assertEquals(task.getStartTime(), taskFromServer.getStartTime(), "Сроки старта должны быть одинаковыми");
    }
}

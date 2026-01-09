package ru.yandex.javacourse.schedule.http.handlers;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.tokens.SubtaskListTypeToken;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubtasksHandlerTest extends BaseHttpHandlerTest {
    public SubtasksHandlerTest() {
        super("/subtasks");
    }

    @Test
    public void getSubtasks_shouldReturnSubtasksFromManager() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(1, "Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        HttpRequest request = buildGetRequest("");
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

        HttpRequest request = buildGetRequest("/1");
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

        HttpRequest request = buildGetRequest("/4");
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

        HttpRequest request = buildGetRequest("/2dsfs");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    public void postSubtask_shouldCreateNewSubtask_noIdProvided() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test", "Subtask task",
                TaskStatus.NEW, 2, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);

        HttpRequest request = buildPostRequest(gson.toJson(subtask));
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

        HttpRequest request = buildPostRequest(gson.toJson(subtask2));
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

        HttpRequest request = buildPostRequest(gson.toJson(subtask2));
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

        HttpRequest request = buildPostRequest(gson.toJson(subtask2));
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void deleteSubtask_shouldDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test", "Subtask task",
                TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now());
        Epic epic = new Epic( "Epic", "Epic description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        HttpRequest request = buildDeleteRequest("/2");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask subtaskFromServer = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), subtaskFromServer.getId(), "Id должны быть равны");
        assertEquals(subtask.getName(), subtaskFromServer.getName(), "Имена должны быть одинаковыми");
        assertEquals(subtask.getDescription(), subtaskFromServer.getDescription(), "Описания должны быть одинаковыми");
        assertEquals(subtask.getDuration(), subtaskFromServer.getDuration(), "Продолжительности должны быть одинаковыми");
        assertEquals(subtask.getStartTime(), subtaskFromServer.getStartTime(), "Сроки старта должны быть одинаковыми");
    }
}

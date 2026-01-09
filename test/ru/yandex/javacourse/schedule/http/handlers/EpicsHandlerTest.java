package ru.yandex.javacourse.schedule.http.handlers;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.tokens.EpicListTypeToken;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EpicsHandlerTest extends BaseHttpHandlerTest {
    public EpicsHandlerTest() {
        super("/epics");
    }

    @Test
    public void getEpics_shouldReturnEpicsFromManager() throws IOException, InterruptedException {
        Epic epic = new Epic("Test", "Testing task");
        manager.addNewEpic(epic);

        HttpRequest request = buildGetRequest("");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> epics = gson.fromJson(response.body(), new EpicListTypeToken().getType());

        assertNotNull(epics, "Задачи не возвращаются");
        assertEquals(1, epics.size(), "Некорректное количество задач");
        assertEquals("Test", epics.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void getEpics_shouldReturnEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic(1, "Test", "Testing task");
        manager.addNewEpic(epic);

        HttpRequest request = buildGetRequest("/1");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertEquals(200, response.statusCode());

        Epic epicFromServer = gson.fromJson(response.body(), Epic.class);

        assertEquals(epic.getId(), epicFromServer.getId(), "Id задачи должен совпадать");
        assertEquals(epic.getName(), epicFromServer.getName(), "Имя задачи должно совпадать");
        assertEquals(epic.getDescription(), epicFromServer.getDescription(), "Описание задачи должно совпадать");
        assertEquals(epic.getStatus(), epicFromServer.getStatus(), "Статус задачи должно совпадать");
        assertEquals(epic.getStartTime(), epicFromServer.getStartTime(), "Старт задачи должно совпадать");
        assertEquals(epic.getDuration(), epicFromServer.getDuration(), "Срок задачи должно совпадать");
    }

    @Test
    public void getEpics_shouldReturn404_noSuchId() throws IOException, InterruptedException {
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);

        HttpRequest request = buildGetRequest("/4");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void getEpics_shouldReturn400_badId() throws IOException, InterruptedException {
        Epic epic = new Epic(2, "Epic", "Epic description");
        manager.addNewEpic(epic);

        HttpRequest request = buildGetRequest("/2dsfs");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    public void postEpics_shouldAddNewEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");

        assertEquals(0, manager.getEpics().size(), "Не должно быть эпиков");

        HttpRequest request = buildPostRequest(gson.toJson(epic));
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Epic managerEpic = manager.getEpic(1);
        assertEquals(epic.getName(), managerEpic.getName(), "Имена должны совпадать");
        assertEquals(epic.getDescription(), managerEpic.getDescription(), "Описания должны совпадать");
    }

    @Test
    public void deleteEpics_shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        manager.addNewEpic(epic);

        HttpRequest request = buildDeleteRequest("/1");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }
}

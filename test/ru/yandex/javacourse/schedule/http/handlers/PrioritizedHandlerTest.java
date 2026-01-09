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

public class PrioritizedHandlerTest extends BaseHttpHandlerTest {
    public PrioritizedHandlerTest() {
        super("/prioritized");
    }

    @Test
    public void getHistory_shouldReturnHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Test 1", "Testing task 1",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Task task2 = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, null, null);
        manager.addNewTask(task1);
        manager.addNewTask(task2);

        HttpRequest request = buildGetRequest("");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(prioritized, "Задачи не возвращаются");
        assertEquals(1, prioritized.size(), "Некорректное количество задач");
        assertEquals("Test 1", prioritized.getFirst().getName(), "Некорректное имя задачи");
    }
}

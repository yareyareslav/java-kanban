package ru.yandex.javacourse.schedule.http;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacourse.schedule.http.handlers.*;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.DONE;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

public class HttpTaskServer {
    private static final int PORT = 8081;

    private static void fillManager(TaskManager manager) {
        Task extraTask1 = new Task("Task #1", "Task1 description", NEW, null, null);
        Task extraTask2 = new Task("Task #2", "Task2 description", NEW, Duration.ofMinutes(50), LocalDateTime.of(2017, 12, 30, 12, 50));
        manager.addNewTask(extraTask1);
        manager.addNewTask(extraTask2);

        Epic extraEpic1 = new Epic("Epic #1", "Epic1 with 3 subtasks");
        Epic extraEpic2 = new Epic("Epic #2", "Epic2 without any subtasks");
        manager.addNewEpic(extraEpic1);
        manager.addNewEpic(extraEpic2);

        int extraEpic1Id = extraEpic1.getId();
        Subtask extraSubtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, extraEpic1Id, Duration.ofMinutes(70), LocalDateTime.of(2015, 12, 30, 12, 50));
        Subtask extraSubtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, extraEpic1Id, Duration.ofMinutes(70), LocalDateTime.of(2016, 12, 30, 12, 50));
        Subtask extraSubtask3 = new Subtask("Subtask #3-1", "Subtask1 description", DONE, extraEpic1Id, Duration.ofMinutes(70), LocalDateTime.of(2018, 12, 30, 12, 50));
        manager.addNewSubtask(extraSubtask1);
        manager.addNewSubtask(extraSubtask2);
        manager.addNewSubtask(extraSubtask3);
    }

    public static void start() throws IOException {
        TaskManager manager = Managers.getDefault();
        fillManager(manager);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/subtasks", new SubtasksHandler(manager));
        server.createContext("/epics", new EpicsHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));

        server.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public static void main(String[] args) throws IOException {
        start();
    }
}

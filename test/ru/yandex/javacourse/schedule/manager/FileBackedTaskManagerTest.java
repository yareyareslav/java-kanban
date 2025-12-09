package ru.yandex.javacourse.schedule.manager;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {
    private File file;
    private FileBackedTaskManager backedManager;

    @BeforeEach
    public void initManager() {
        try {
            file = File.createTempFile("autosave", ".txt");
            file.deleteOnExit();
            manager = Managers.getDefault();
            backedManager = Managers.getFileBacked(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void addEntities_shouldPersistAllToFile() throws IOException {
        Task task = new Task("Task 1", "Desc 1", TaskStatus.NEW);
        Epic epic = new Epic("Epic 1", "Desc epic");

        backedManager.addNewTask(task);
        backedManager.addNewEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Desc sub", TaskStatus.IN_PROGRESS, epic.getId());
        backedManager.addNewSubtask(subtask);

        List<String> lines = Files.readAllLines(file.toPath());

        assertEquals(4, lines.size(), "Файл должен содержать заголовок и три строки сущностей");
        assertEquals("id,type,name,status,description,epic", lines.getFirst(), "Ожидается заголовок CSV");
        assertTrue(lines.contains("1,TASK,Task 1,NEW,Desc 1,"), "Файл должен содержать строку задачи");
        assertTrue(lines.contains("2,EPIC,Epic 1,IN_PROGRESS,Desc epic,"), "Файл должен содержать строку эпика");
        assertTrue(lines.contains("3,SUBTASK,Subtask 1,IN_PROGRESS,Desc sub,2"), "Файл должен содержать строку подзадачи");
    }


    @Test
    public void loadFromFile_shouldRestoreTasksEpicsAndSubtasks() throws IOException {
        File source = File.createTempFile("autosave-load", ".txt");
        source.deleteOnExit();
        String fileContent = String.join("\r\n",
                "id,type,name,status,description,epic",
                "7,TASK,Persisted task,NEW,Task description,",
                "8,EPIC,Persisted epic,NEW,Epic description,",
                "9,SUBTASK,Persisted subtask,DONE,Sub description,8"
        );
        Files.writeString(source.toPath(), fileContent);

        backedManager.loadFromFile(source);

        assertEquals(1, backedManager.getTasks().size(), "Должна загрузиться одна задача");
        assertEquals(1, backedManager.getEpics().size(), "Должен загрузиться один эпик");
        assertEquals(1, backedManager.getSubtasks().size(), "Должна загрузиться одна подзадача");

        Task loadedTask = backedManager.getTask(7);
        Epic loadedEpic = backedManager.getEpic(8);
        Subtask loadedSubtask = backedManager.getSubtask(9);

        assertEquals("Persisted task", loadedTask.getName(), "Имя задачи должно совпадать");
        assertEquals("Persisted epic", loadedEpic.getName(), "Имя эпика должно совпадать");
        assertEquals("Persisted subtask", loadedSubtask.getName(), "Имя подзадачи должно совпадать");
        assertEquals(loadedEpic.getId(), loadedSubtask.getEpicId(), "Связь подзадачи с эпиком должна сохраниться");
    }

    @Test
    public void deleteTask_task1ShouldNotBeInNewManager() throws IOException {
        Task task1 = new Task(1,"Task 1", "Desc 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Desc 2", TaskStatus.NEW);

        backedManager.addNewTask(task1);
        backedManager.addNewTask(task2);

        assertEquals(2, backedManager.getTasks().size(), "Должно быть две задачи");

        backedManager.deleteTask(task1.getId());

        assertEquals(1, backedManager.getTasks().size(), "Должна быть одна задача");

        File newFile = File.createTempFile("autosave", ".txt");
        newFile.deleteOnExit();
        FileBackedTaskManager newManager = new FileBackedTaskManager(newFile);
        newManager.loadFromFile(file);

        assertEquals(1, newManager.getTasks().size(), "Должна быть одна задача");
        assertEquals(List.of(task2), newManager.getTasks(), "Должна быть задача с id 2");
    }

    @Test
    public void deleteTask_changesShouldBeInTheNewManager() throws IOException {
        Task task = new Task(1,"Task", "Desc Task", TaskStatus.NEW);
        Epic epic = new Epic(3, "Epic", "Desc Epic");
        Subtask subtask = new Subtask(2, "Subtask", "Desc Subtask", TaskStatus.NEW, 3);


        backedManager.addNewTask(task);
        backedManager.addNewEpic(epic);
        backedManager.addNewSubtask(subtask);

        assertEquals(1, backedManager.getTasks().size(), "Должна быть одна задача");
        assertEquals(1, backedManager.getEpics().size(), "Должен быть один эпик");
        assertEquals(1, backedManager.getSubtasks().size(), "Должна быть одна подзадача");

        backedManager.deleteTask(task.getId());

        assertEquals(0, backedManager.getTasks().size(), "Задач быть не должно");

        File newFile = File.createTempFile("autosave", ".txt");
        newFile.deleteOnExit();
        FileBackedTaskManager newManager = new FileBackedTaskManager(newFile);
        newManager.loadFromFile(file);

        assertEquals(0, newManager.getTasks().size(), "Задач быть не должно");
        assertEquals(1, newManager.getEpics().size(), "Должен быть один эпик");
        assertEquals(1, newManager.getSubtasks().size(), "Должна быть одна подзадача");
        assertEquals(epic, newManager.getEpic(3), "Должен быть один эпик");
        assertEquals(subtask, newManager.getSubtask(2), "Должна быть одна подзадача");

    }
}

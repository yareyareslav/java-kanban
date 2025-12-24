package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {
    private File file;

    @BeforeEach
    public void initManager() {
        try {
            file = File.createTempFile("autosave", ".txt");
            file.deleteOnExit();
            manager = FileBackedTaskManager.loadFromFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void addEntities_shouldPersistAllToFile() throws IOException {
        Task task = new Task("Task 1", "Desc 1", TaskStatus.NEW);
        Epic epic = new Epic("Epic 1", "Desc epic");

        manager.addNewTask(task);
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Desc sub", TaskStatus.IN_PROGRESS, epic.getId());
        manager.addNewSubtask(subtask);

        List<String> lines = Files.readAllLines(file.toPath());

        assertEquals(4, lines.size(), "Файл должен содержать заголовок и три строки сущностей");
        assertEquals(TaskCsvConverter.getHeading(), lines.getFirst(), "Ожидается заголовок CSV");
        assertTrue(lines.contains("1,TASK,Task 1,NEW,Desc 1,null,null,null"), "Файл должен содержать строку задачи");
        assertTrue(lines.contains("2,EPIC,Epic 1,IN_PROGRESS,Desc epic,null,null,null"), "Файл должен содержать строку эпика");
        assertTrue(lines.contains("3,SUBTASK,Subtask 1,IN_PROGRESS,Desc sub,2,null,null"), "Файл должен содержать строку подзадачи");
    }


    @Test
    public void loadFromFile_shouldRestoreTasksEpicsAndSubtasks() throws IOException {
        File source = File.createTempFile("autosave-load", ".txt");
        source.deleteOnExit();
        String fileContent = String.join("\r\n",
                TaskCsvConverter.getHeading(),
                "7,TASK,Persisted task,NEW,Task description,null,null,null",
                "8,EPIC,Persisted epic,NEW,Epic description,null,null,null",
                "9,SUBTASK,Persisted subtask,DONE,Sub description,8,null,null"
        );
        Files.writeString(source.toPath(), fileContent);

        manager = FileBackedTaskManager.loadFromFile(source);

        assertEquals(1, manager.getTasks().size(), "Должна загрузиться одна задача");
        assertEquals(1, manager.getEpics().size(), "Должен загрузиться один эпик");
        assertEquals(1, manager.getSubtasks().size(), "Должна загрузиться одна подзадача");

        Task loadedTask = manager.getTask(7).get();
        Epic loadedEpic = manager.getEpic(8).get();
        Subtask loadedSubtask = manager.getSubtask(9).get();

        assertEquals("Persisted task", loadedTask.getName(), "Имя задачи должно совпадать");
        assertEquals("Persisted epic", loadedEpic.getName(), "Имя эпика должно совпадать");
        assertEquals("Persisted subtask", loadedSubtask.getName(), "Имя подзадачи должно совпадать");
        assertEquals(loadedEpic.getId(), loadedSubtask.getEpicId(), "Связь подзадачи с эпиком должна сохраниться");
    }

    @Test
    public void deleteTask_task1ShouldNotBeInNewManager() throws IOException {
        Task task1 = new Task(1,"Task 1", "Desc 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Desc 2", TaskStatus.NEW);

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        assertEquals(2, manager.getTasks().size(), "Должно быть две задачи");

        manager.deleteTask(task1.getId());

        assertEquals(1, manager.getTasks().size(), "Должна быть одна задача");

        File newFile = File.createTempFile("autosave", ".txt");
        newFile.deleteOnExit();
        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, newManager.getTasks().size(), "Должна быть одна задача");
        assertEquals(List.of(task2), newManager.getTasks(), "Должна быть задача с id 2");
    }

    @Test
    public void deleteTask_changesShouldBeInTheNewManager() throws IOException {
        Task task = new Task(1,"Task", "Desc Task", TaskStatus.NEW);
        Epic epic = new Epic(3, "Epic", "Desc Epic");
        Subtask subtask = new Subtask(2, "Subtask", "Desc Subtask", TaskStatus.NEW, 3);


        manager.addNewTask(task);
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        assertEquals(1, manager.getTasks().size(), "Должна быть одна задача");
        assertEquals(1, manager.getEpics().size(), "Должен быть один эпик");
        assertEquals(1, manager.getSubtasks().size(), "Должна быть одна подзадача");

        manager.deleteTask(task.getId());

        assertEquals(0, manager.getTasks().size(), "Задач быть не должно");

        File newFile = File.createTempFile("autosave", ".txt");
        newFile.deleteOnExit();
        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(0, newManager.getTasks().size(), "Задач быть не должно");
        assertEquals(1, newManager.getEpics().size(), "Должен быть один эпик");
        assertEquals(1, newManager.getSubtasks().size(), "Должна быть одна подзадача");
        assertEquals(epic, newManager.getEpic(3).get(), "Должен быть один эпик");
        assertEquals(subtask, newManager.getSubtask(2).get(), "Должна быть одна подзадача");

    }

    @Test
    public void loadFromFile_taskIdsShouldBeDifferent_addTaskWithExistingId() {
        Task task1 = new Task(1,"Task 1", "Desc Task", TaskStatus.NEW);

        manager.addNewTask(task1);

        TaskManager newManager = Managers.getFileBacked(file);
        Task task2 = new Task(1,"Task 2", "Desc Task", TaskStatus.NEW);
        newManager.addNewTask(task2);

        assertEquals(1, manager.getTasks().size(), "В старом менеджере должна быть 1 задача");
        assertEquals(2, newManager.getTasks().size(), "В новом менеджере должно быть 2 задачи");
        assertEquals(1, task1.getId(), "Id 1-й задачи должен остаться 1");
        assertEquals(2, task2.getId(), "Id 2-й задачи должен переопределиться на 2");
    }

    @Test
    public void loadFromFile_epicsShouldHaveSubtasks() {
        Epic epic = new Epic(1, "Epic", "Desc Epic");
        Subtask subtask1 = new Subtask(2,"Task 1", "Desc Task", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask(2,"Task 2", "Desc Task", TaskStatus.NEW, 1);

        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        TaskManager newManager = Managers.getFileBacked(file);

        assertEquals(2, newManager.getEpic(epic.getId()).get().getSubtaskIds().size(), "В эпике должно быть 2 подзадачи");
    }

    @Test
    public void loadFromFile_shouldRestoreTasksWithTime() {
        super.getEndTime_shouldCalcEndTime_ifStartTimeAndDurationAreProvided();

        TaskManager newManager = Managers.getFileBacked(file);
        Task task = newManager.getTask(1).get();
        Epic epic = newManager.getEpic(2).get();
        Subtask subtask = newManager.getSubtask(3).get();

        assertEquals(manager.getTask(1).get(), task, "Задача должно быть равен предыдущей версии");
        assertEquals(manager.getEpic(2).get(), epic, "Эпик должен быть равен предыдущей версии");
        assertEquals(manager.getSubtask(3).get(), subtask, "Подзадача должно быть равен предыдущей версии");
    }

    @Test
    public void getEndTime_shouldRestoreTasksWithTime() {
        super.getEndTime_shouldCalcEndTime_ifStartTimeAndDurationAreProvided();

        TaskManager newManager = Managers.getFileBacked(file);
        Task task = newManager.getTask(1).get();
        Epic epic = newManager.getEpic(2).get();
        Subtask subtask = newManager.getSubtask(3).get();

        assertEquals(manager.getTask(1).get().getEndTime().get(), task.getEndTime().get(), "Задача должна заканчиваться в то же время");
        assertEquals(manager.getEpic(2).get().getEndTime().get(), epic.getEndTime().get(), "Эпик должен заканчиваться в то же время");
        assertEquals(manager.getSubtask(3).get().getEndTime().get(), subtask.getEndTime().get(), "Подзадача должна заканчиваться в то же время");
    }
}

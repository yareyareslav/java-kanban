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
            manager = Managers.getFileBacked(file);
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
        assertEquals("id,type,name,status,description,epic", lines.getFirst(), "Ожидается заголовок CSV");
        assertTrue(lines.contains("1,TASK,Task 1,NEW,Desc 1,"), "Файл должен содержать строку задачи");
        assertTrue(lines.contains("2,EPIC,Epic 1,IN_PROGRESS,Desc epic,"), "Файл должен содержать строку эпика");
        assertTrue(lines.contains("3,SUBTASK,Subtask 1,IN_PROGRESS,Desc sub,2"), "Файл должен содержать строку подзадачи");
    }


    @Test
    public void loadFromFile_shouldRestoreTasksEpicsAndSubtasks() throws IOException {
        super.loadFromFile_shouldRestoreTasksEpicsAndSubtasks();
    }
}

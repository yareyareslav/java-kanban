package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager(){
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testHistoricVersions(){
        Task task = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        historyManager.add(task);
        assertEquals(1, historyManager.getTasks().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.add(task);
        assertEquals(2, historyManager.getTasks().size(), "historic task should be added");
    }

    @Test
    public void testHistoricVersionsByPointer(){
        Task task = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        historyManager.add(task);
        assertEquals(task.getStatus(), historyManager.getTasks().get(0).getStatus(), "historic task should be stored");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.add(task);
        assertEquals(TaskStatus.NEW, historyManager.getTasks().get(0).getStatus(), "historic task should not be changed");
    }

}

package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager(){
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testHistoricVersions(){
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        historyManager.add(task);
        assertEquals(1, historyManager.getTasks().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.add(task);
        assertEquals(1, historyManager.getTasks().size(), "historic task should rest the same");
    }

    @Test
    public void testHistoricVersionsByPointer(){
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        historyManager.add(task);
        assertEquals(task.getStatus(), historyManager.getTasks().getFirst().getStatus(), "historic task should be stored");
    }

    @Test
    public void remove_nodeShouldBeDeletedByItsIDFromStart() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        ArrayList<Task> tasks1 = historyManager.getTasks();
        assertEquals(Arrays.asList(task1, task2, task3), tasks1, "history manager should remember the order of tasks");
        historyManager.remove(1);
        ArrayList<Task> tasks2 = historyManager.getTasks();
        assertEquals(Arrays.asList(task2, task3), tasks2, "history manager should not contain task1 in the list");
    }

    @Test
    public void remove_nodeShouldBeDeletedByItsIDFromCenter() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        ArrayList<Task> tasks1 = historyManager.getTasks();
        assertEquals(Arrays.asList(task1, task2, task3), tasks1, "history manager should remember the order of tasks");
        historyManager.remove(2);
        ArrayList<Task> tasks2 = historyManager.getTasks();
        assertEquals(Arrays.asList(task1, task3), tasks2, "history manager should not contain task2 in the list");
    }

    @Test
    public void remove_nodeShouldBeDeletedByItsIDFromEnd() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        ArrayList<Task> tasks1 = historyManager.getTasks();
        assertEquals(Arrays.asList(task1, task2, task3), tasks1, "history manager should remember the order of tasks");
        historyManager.remove(3);
        ArrayList<Task> tasks2 = historyManager.getTasks();
        assertEquals(Arrays.asList(task1, task2), tasks2, "history manager should not contain task3 in the list");
    }

    @Test
    public void getTasks_shouldSaveOrder_ifAddTaskWithTheSameId() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW);
        historyManager.add(task3);
        historyManager.add(task1);
        historyManager.add(task2);
        ArrayList<Task> tasks1 = historyManager.getTasks();
        assertEquals(Arrays.asList(task3, task1, task2), tasks1, "history manager should remember the order of tasks");
        historyManager.add(task3);
        ArrayList<Task> tasks2 = historyManager.getTasks();
        assertEquals(Arrays.asList(task1, task2, task3), tasks2, "history manager should change the position of the task3");
        historyManager.add(task2);
        ArrayList<Task> tasks3 = historyManager.getTasks();
        assertEquals(Arrays.asList(task1, task3, task2), tasks3, "history manager should change the position of the task2");
    }

    @Test
    public void getTasks_shouldRemoveDuplicates_sameTaskIsCalledTwice() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task3);
        ArrayList<Task> tasks1 = historyManager.getTasks();
        assertEquals(Arrays.asList(task1, task2, task3), tasks1, "history manager should remember the order of tasks");
        historyManager.add(task2);
        ArrayList<Task> tasks2 = historyManager.getTasks();
        assertEquals(Arrays.asList(task1, task3, task2), tasks2, "history manager should change the position of the task3");
    }
}

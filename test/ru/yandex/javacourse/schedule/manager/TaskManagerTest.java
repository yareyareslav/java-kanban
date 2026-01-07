package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    @BeforeEach
    public void setUp() {
        manager = createTaskManager();
    }

    protected abstract T createTaskManager();

    @Test
    public void getTasks_shouldReturnListOfTasks() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW, null, null);

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        assertEquals(2, manager.getTasks().size(), "В списке должно быть две задачи");
        assertEquals(List.of(task1, task2), manager.getTasks(), "Список должен содержать добавленные задачи");
    }

    @Test
    public void getSubtasks_shouldReturnListOfSubtasks() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId(), null, null);
        Subtask subtask2 = new Subtask(2, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic.getId(), null, null);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(2, manager.getSubtasks().size(), "В списке должно быть две подзадачи");
        assertEquals(List.of(subtask1, subtask2), manager.getSubtasks(), "Список должен содержать добавленные подзадачи");
    }

    @Test
    public void getEpics_shouldReturnListOfEpics() {
        Epic epic1 = new Epic(1, "Test 1", "Testing epic1");
        Epic epic2 = new Epic(2, "Test 2", "Testing epic2");
        manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);


        assertEquals(2, manager.getEpics().size(), "В списке должно быть два эпика");
        assertEquals(List.of(epic1, epic2), manager.getEpics(), "Список должен содержать добавленные эпики");
    }

    @Test
    public void getEpicSubtasks_shouldReturnSubtasksOfProvidedEpic() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId(), null, null);
        Subtask subtask2 = new Subtask(2, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic.getId(), null, null);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(2, manager.getEpicSubtasks(epic.getId()).size(), "В списке должно быть две подзадачи");
        assertEquals(List.of(subtask1, subtask2), manager.getEpicSubtasks(epic.getId()), "Список должен содержать добавленные подзадачи");
    }

    @Test
    public void getTask_shouldReturnTask_taskWithIdExists() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);

        manager.addNewTask(task1);

        assertSame(task1, manager.getTask(task1.getId()), "Должен вернуть задачу по id");
    }

    @Test
    public void getTask_shouldReturnEmpty_taskWithIdDoesNotExist() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);

        manager.addNewTask(task1);

        assertThrows(NotFoundException.class, () -> manager.getTask(1000));
    }

    @Test
    public void getSubtask_shouldReturnSubtask_subtaskWithIdExists() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId(), null, null);

        manager.addNewTask(subtask1);

        assertSame(subtask1, manager.getTask(subtask1.getId()), "Должен вернуть подзадачу по id");
    }

    @Test
    public void getSubtask_shouldReturnEmpty_subtaskWithIdDoesNotExist() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId(), null, null);

        manager.addNewTask(subtask1);

        assertThrows(NotFoundException.class, () -> manager.getTask(1000), "Должен вернуть empty");
    }

    @Test
    public void getEpic_shouldReturnEpic_epicWithIdExists() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");

        manager.addNewEpic(epic);

        assertSame(epic, manager.getEpic(epic.getId()), "Должен вернуть эпик по id");
    }

    @Test
    public void getEpic_shouldReturnEpic_epicWithIdDoesNotExist() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");

        manager.addNewEpic(epic);

        assertThrows(NotFoundException.class, () -> manager.getTask(1000));
    }

    @Test
    public void addNewTask_shouldAddNewTaskToManager() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);

        int id = manager.addNewTask(task1);

        assertSame(task1, manager.getTask(id), "Задача должна быть в менеджере");
    }

    @Test
    public void addNewTask_shouldAssignIdToTask2_taskIdMissing(){
        Task task1 = new Task(2, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW, null, null);
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        assertEquals(2, manager.getTasks().size(), "the same size of tasks in manager");
        assertEquals(2, task1.getId(), "task predefined id should not change");
        assertEquals(1, task2.getId(), "autogenerated id should be 1");
    }

    @Test
    public void addNewTask_shouldRewriteIdOfTask2_tasksHaveSameIds() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        Task task2 = new Task(1, "Test 2", "Testing task 2", TaskStatus.NEW, null, null);

        manager.addNewTask(task1);
        int id = manager.addNewTask(task2);

        assertEquals(2, id, "Id должен поменяться на 2");
    }

    @Test
    public void addNewSubtask_shouldAddNewSubtaskToManager() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId(), null, null);
        int id = manager.addNewSubtask(subtask1);

        assertSame(subtask1, manager.getSubtask(id), "Подзадача должна быть в менеджере");
    }

    @Test
    public void addNewSubtask_shouldRewriteIdOfSubtask2_tasksHaveSameIds() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId(), null, null);
        Subtask subtask2 = new Subtask(1, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic.getId(), null, null);
        manager.addNewSubtask(subtask1);
        int id = manager.addNewSubtask(subtask2);

        assertEquals(2, id, "Id должен поменяться на 2");
    }

    @Test
    public void addNewSubtask_shouldChangeSubtaskId_idEqualsToEpicId() {
        Epic epic = new Epic(1, "Test 1", "Testing epic for subtasks");
        Subtask subtask = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, 1, null, null);
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        assertNotEquals(epic.getId(), subtask.getId(), "Subtask id should not be equal to epicId");
    }

    @Test
    public void addNewSubtask_shouldThrow_epicDoesNotExist() {
        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, 1000, null, null);

        assertThrows(NotFoundException.class, () -> manager.addNewSubtask(subtask1));
    }

    @Test
    public void addNewEpic_shouldAddNewEpicToManager() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        int id = manager.addNewEpic(epic);

        assertSame(epic, manager.getEpic(id), "Эпик должен быть в менеджере");
    }

    @Test
    public void addNewEpic_shouldRewriteIdOfEpic2_tasksHaveSameIds() {
        Epic epic1 = new Epic(1, "Test 1", "Testing epic1 for subtasks");
        Epic epic2 = new Epic(1, "Test 2", "Testing epic2 for subtasks");
        manager.addNewEpic(epic1);
        int id = manager.addNewEpic(epic2);

        assertEquals(2, id, "Id должен поменяться на 2");
    }
    
    @Test
    public void updateTask_shouldUpdateTask_taskWithIdExists() {
        Task taskOld = new Task(1, "Test Old", "Testing task Old", TaskStatus.NEW, null, null);
        Task taskNew = new Task(1, "Test New", "Testing task New", TaskStatus.NEW, null, null);

        manager.addNewTask(taskOld);
        assertSame(taskOld, manager.getTask(taskOld.getId()), "Задача должна добавиться");

        manager.updateTask(taskNew);
        assertSame(taskNew, manager.getTask(taskOld.getId()), "Задача должна обновиться");
        assertNotSame(taskOld, manager.getTask(taskOld.getId()), "Старой задачи не должно быть в менеджере");
    }

    @Test
    public void updateTask_shouldThrowNotFoundException_taskWithIdDoesNotExist() {
        Task taskOld = new Task(1, "Test Old", "Testing task Old", TaskStatus.NEW, null, null);
        Task taskNew = new Task(2, "Test New", "Testing task New", TaskStatus.NEW, null, null);

        manager.addNewTask(taskOld);
        assertSame(taskOld, manager.getTask(taskOld.getId()), "Задача должна добавиться");

        assertThrows(NotFoundException.class, () -> manager.updateTask(taskNew));
    }

    @Test
    public void updateEpic_shouldUpdateEpic_epicWithIdExists() {
        Epic epicOld = new Epic(1, "Test Old", "Testing epic Old");
        Epic epicNew = new Epic(1, "Test New", "Testing epic New");

        manager.addNewEpic(epicOld);
        assertSame(epicOld, manager.getEpic(epicOld.getId()), "Эпик должен добавиться");

        manager.updateEpic(epicNew);
        assertEquals(epicOld.getName(), epicNew.getName(), "Имя эпика должно обновиться");
        assertEquals(epicOld.getDescription(), epicNew.getDescription(), "Описание эпика должно обновиться");
        assertSame(epicOld, manager.getEpic(epicOld.getId()), "Эпик должен остаться тем же объектом в менеджере");
    }

    @Test
    public void updateEpic_shouldThrowNotFoundException_epicWithIdDoesNotExist() {
        Epic epicOld = new Epic(1, "Test Old", "Testing epic Old");
        Epic epicNew = new Epic(2, "Test New", "Testing epic New");

        manager.addNewEpic(epicOld);
        assertSame(epicOld, manager.getEpic(epicOld.getId()), "Эпик должен добавиться");

        assertThrows(NotFoundException.class, () -> manager.updateEpic(epicNew));
    }

    @Test
    public void updateSubtask_shouldUpdateSubtask_subtaskWithIdExists() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtaskOld = new Subtask(1, "Test Old", "Testing subtask Old", TaskStatus.NEW, 3, null, null);
        Subtask subtaskNew = new Subtask(1, "Test New", "Testing subtask New", TaskStatus.NEW, 3, null, null);

        manager.addNewSubtask(subtaskOld);
        assertSame(subtaskOld, manager.getSubtask(subtaskOld.getId()), "Подзадача должна добавиться");

        manager.updateSubtask(subtaskNew);
        assertSame(subtaskNew, manager.getSubtask(subtaskOld.getId()), "Подзадача должна обновиться");
    }

    @Test
    public void updateSubtask_shouldThrowNotFoundException_subtaskWithIdDoesNotExist() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtaskOld = new Subtask(1, "Test Old", "Testing subtask Old", TaskStatus.NEW, 3, null, null);
        Subtask subtaskNew = new Subtask(2, "Test New", "Testing subtask New", TaskStatus.NEW, 3, null, null);

        manager.addNewSubtask(subtaskOld);
        assertSame(subtaskOld, manager.getSubtask(subtaskOld.getId()), "Подзадача должна добавиться");

        assertThrows(NotFoundException.class, () -> manager.updateSubtask(subtaskNew));
    }

    @Test
    public void updateSubtask_shouldThrowNotFoundException_epicWithIdDoesNotExist() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtaskOld = new Subtask(1, "Test Old", "Testing subtask Old", TaskStatus.NEW, 3, null, null);
        Subtask subtaskNew = new Subtask(1, "Test New", "Testing subtask New", TaskStatus.NEW, 4, null, null);

        manager.addNewSubtask(subtaskOld);
        assertSame(subtaskOld, manager.getSubtask(subtaskOld.getId()), "Подзадача должна добавиться");

        assertThrows(NotFoundException.class, () -> manager.updateSubtask(subtaskNew));
    }

    @Test
    public void deleteTask_shouldDeleteTask_taskExists() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        int id = manager.addNewTask(task);

        assertSame(task, manager.getTask(id), "Задача должна добавиться в менеджер");

        manager.deleteTask(id);
        assertEquals(0, manager.getTasks().size(), "Список задач должен быть пуст");
    }

    @Test
    public void deleteTask_shouldNotDeleteTask_taskDoesNotExist() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        int id = manager.addNewTask(task);

        assertSame(task, manager.getTask(id), "Задача должна добавиться в менеджер");

        assertThrows(NotFoundException.class, () -> manager.deleteTask(1000));
        assertEquals(1, manager.getTasks().size(), "Список задач должен иметь одну задачу");
    }

    @Test
    public void deleteEpic_shouldDeleteEpic_epicExists() {
        Epic epic = new Epic(1, "Epic 1", "Testing task 1");
        int id = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(2, "Test 1", "Testing subtask 1", TaskStatus.NEW, id, null, null);
        Subtask subtask2 = new Subtask(3, "Test 2", "Testing subtask 2", TaskStatus.NEW, id, null, null);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(epic, manager.getEpic(id), "Эпик должен добавиться в менеджер");
        assertEquals(2, manager.getEpicSubtasks(id).size(), "В списке подзадач эпика должно быть 2 подзадачи");

        manager.deleteEpic(id);
        assertEquals(0, manager.getEpics().size(), "Список эпиков должен быть пуст");
        assertEquals(0, manager.getSubtasks().size(), "Список подзадач должен быть пуст");
    }

    @Test
    public void deleteEpic_shouldNotDeleteEpic_epicDoesNotExist() {
        Epic epic = new Epic(1, "Epic 1", "Testing task 1");
        int id = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(2, "Test 1", "Testing subtask 1", TaskStatus.NEW, id, null, null);
        Subtask subtask2 = new Subtask(3, "Test 2", "Testing subtask 2", TaskStatus.NEW, id, null, null);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertSame(epic, manager.getEpic(id), "Эпик должен добавиться в менеджер");

        assertThrows(NotFoundException.class, () -> manager.deleteEpic(1000));
        assertEquals(1, manager.getEpics().size(), "Список эпиков должен иметь один эпик");
        assertEquals(2, manager.getSubtasks().size(), "В списке подзадач должно быть две подзадачи");
    }

    @Test
    public void deleteSubtask_shouldDeleteSubtask_subtaskExists() {
        Epic epic = new Epic(1, "Epic 1", "Testing task 1");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(2, "Test 1", "Testing subtask 1", TaskStatus.NEW, epicId, null, null);
        Subtask subtask2 = new Subtask(3, "Test 2", "Testing subtask 2", TaskStatus.NEW, epicId, null, null);
        manager.addNewSubtask(subtask1);
        int id = manager.addNewSubtask(subtask2);

        assertSame(epic, manager.getEpic(epicId), "Эпик должен добавиться в менеджер");
        assertEquals(2, manager.getEpicSubtasks(epicId).size(), "В списке подзадач эпика должно быть 2 подзадачи");

        manager.deleteSubtask(id);
        assertEquals(1, manager.getEpics().size(), "Эпик должен остаться");
        assertEquals(1, manager.getSubtasks().size(), "Должна быть одна подзадача");
    }

    @Test
    public void deleteSubtask_shouldNotDeleteSubtask_subtaskDoesNotExist() {
        Epic epic = new Epic(1, "Epic 1", "Testing task 1");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(2, "Test 1", "Testing subtask 1", TaskStatus.NEW, epicId, null, null);
        Subtask subtask2 = new Subtask(3, "Test 2", "Testing subtask 2", TaskStatus.NEW, epicId, null, null);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertSame(epic, manager.getEpic(epicId), "Эпик должен добавиться в менеджер");
        assertEquals(2, manager.getEpicSubtasks(epicId).size(), "В списке подзадач эпика должно быть 2 подзадачи");

        assertThrows(NotFoundException.class, () -> manager.deleteSubtask(1000));
        assertEquals(1, manager.getEpics().size(), "Эпик должен остаться");
        assertEquals(2, manager.getSubtasks().size(), "В списке подзадач должно быть 2 подзадачи");
    }

    @Test
    public void deleteTasks_shouldDeleteAllTasks() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW, null, null);

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        assertEquals(2, manager.getTasks().size(), "В менеджере должно быть 2 задачи");

        manager.deleteTasks();

        assertEquals(0, manager.getTasks().size(), "В менеджере не должно быть задач");
    }

    @Test
    public void deleteSubtasks_shouldDeleteAllSubtasks() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId(), null, null);
        Subtask subtask2 = new Subtask(2, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic.getId(), null, null);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(2, manager.getSubtasks().size(), "В менеджере должно быть 2 подзадачи");

        manager.deleteSubtasks();

        assertEquals(0, manager.getSubtasks().size(), "В менеджере не должно быть подзадач");
    }

    @Test
    public void deleteEpics_shouldDeleteEpics() {
        Epic epic1 = new Epic(1, "Test 1", "Testing epic1");
        Epic epic2 = new Epic(2, "Test 2", "Testing epic2");
        manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);
        Subtask subtask1 = new Subtask(3, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic1.getId(), null, null);
        Subtask subtask2 = new Subtask(4, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic1.getId(), null, null);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(2, manager.getEpics().size(), "В менеджере должно быть 2 эпика");

        manager.deleteEpics();

        assertEquals(0, manager.getEpics().size(), "В менеджере не должно быть эпиков");
        assertEquals(0, manager.getSubtasks().size(), "В менеджере не должно быть подзадач");
    }

    @Test
    public void getHistory_shouldReturnViewsHistory() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW, null, null);
        Task task3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW, null, null);

        int id1 = manager.addNewTask(task1);
        int id2 = manager.addNewTask(task2);
        manager.addNewTask(task3);

        assertEquals(3, manager.getTasks().size(), "В менеджере должно быть 3 задачи");

        manager.getTask(id1);
        manager.getTask(id2);

        assertEquals(2, manager.getHistory().size(), "В истории просмотров должно быть две задачи");
        assertEquals(new ArrayList<>(List.of(task1, task2)), manager.getHistory(), "В истории должны быть задача 1 и задача 2");
    }

}

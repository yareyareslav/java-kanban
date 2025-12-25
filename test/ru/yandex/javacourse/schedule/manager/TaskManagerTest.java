package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.exceptions.EpicDoesNotExist;
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
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        assertEquals(2, manager.getTasks().size(), "В списке должно быть две задачи");
        assertEquals(List.of(task1, task2), manager.getTasks(), "Список должен содержать добавленные задачи");
    }

    @Test
    public void getSubtasks_shouldReturnListOfSubtasks() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(2, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic.getId());
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

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(2, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(2, manager.getEpicSubtasks(epic.getId()).size(), "В списке должно быть две подзадачи");
        assertEquals(List.of(subtask1, subtask2), manager.getEpicSubtasks(epic.getId()), "Список должен содержать добавленные подзадачи");
    }

    @Test
    public void getTask_shouldReturnTask_taskWithIdExists() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);

        manager.addNewTask(task1);

        assertSame(task1, manager.getTask(task1.getId()).get(), "Должен вернуть задачу по id");
    }

    @Test
    public void getTask_shouldReturnEmpty_taskWithIdDoesNotExist() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);

        manager.addNewTask(task1);

        assertEquals(Optional.empty(), manager.getTask(1000), "Должен вернуть empty");
    }

    @Test
    public void getSubtask_shouldReturnSubtask_subtaskWithIdExists() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId());

        manager.addNewTask(subtask1);

        assertSame(subtask1, manager.getTask(subtask1.getId()).get(), "Должен вернуть подзадачу по id");
    }

    @Test
    public void getSubtask_shouldReturnEmpty_subtaskWithIdDoesNotExist() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId());

        manager.addNewTask(subtask1);

        assertEquals(Optional.empty(), manager.getTask(1000), "Должен вернуть empty");
    }

    @Test
    public void getEpic_shouldReturnEpic_epicWithIdExists() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");

        manager.addNewEpic(epic);

        assertSame(epic, manager.getEpic(epic.getId()).get(), "Должен вернуть эпик по id");
    }

    @Test
    public void getEpic_shouldReturnEpic_epicWithIdDoesNotExist() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");

        manager.addNewEpic(epic);

        assertEquals(Optional.empty(), manager.getTask(1000), "Должен вернуть empty");
    }

    @Test
    public void addNewTask_shouldAddNewTaskToManager() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);

        int id = manager.addNewTask(task1);

        assertSame(task1, manager.getTask(id).get(), "Задача должна быть в менеджере");
    }

    @Test
    public void addNewTask_shouldAssignIdToTask2_taskIdMissing(){
        Task task1 = new Task(2, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        assertEquals(2, manager.getTasks().size(), "the same size of tasks in manager");
        assertEquals(2, task1.getId(), "task predefined id should not change");
        assertEquals(1, task2.getId(), "autogenerated id should be 1");
    }

    @Test
    public void addNewTask_shouldRewriteIdOfTask2_tasksHaveSameIds() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(1, "Test 2", "Testing task 2", TaskStatus.NEW);

        manager.addNewTask(task1);
        int id = manager.addNewTask(task2);

        assertEquals(2, id, "Id должен поменяться на 2");
    }

    @Test
    public void addNewSubtask_shouldAddNewSubtaskToManager() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId());
        int id = manager.addNewSubtask(subtask1);

        assertSame(subtask1, manager.getSubtask(id).get(), "Подзадача должна быть в менеджере");
    }

    @Test
    public void addNewSubtask_shouldRewriteIdOfSubtask2_tasksHaveSameIds() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(1, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic.getId());
        manager.addNewSubtask(subtask1);
        int id = manager.addNewSubtask(subtask2);

        assertEquals(2, id, "Id должен поменяться на 2");
    }

    @Test
    public void addNewSubtask_shouldThrow_epicDoesNotExist() {
        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, 1000);

        assertThrows(EpicDoesNotExist.class, () -> manager.addNewSubtask(subtask1));
    }

    @Test
    public void addNewEpic_shouldAddNewEpicToManager() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        int id = manager.addNewEpic(epic);

        assertSame(epic, manager.getEpic(id).get(), "Эпик должен быть в менеджере");
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
        Task taskOld = new Task(1, "Test Old", "Testing task Old", TaskStatus.NEW);
        Task taskNew = new Task(1, "Test New", "Testing task New", TaskStatus.NEW);

        manager.addNewTask(taskOld);
        assertSame(taskOld, manager.getTask(taskOld.getId()).get(), "Задача должна добавиться");

        manager.updateTask(taskNew);
        assertSame(taskNew, manager.getTask(taskOld.getId()).get(), "Задача должна обновиться");
        assertNotSame(taskOld, manager.getTask(taskOld.getId()).get(), "Старой задачи не должно быть в менеджере");
    }

    @Test
    public void updateTask_shouldNotUpdateTask_taskWithIdDoesNotExist() {
        Task taskOld = new Task(1, "Test Old", "Testing task Old", TaskStatus.NEW);
        Task taskNew = new Task(2, "Test New", "Testing task New", TaskStatus.NEW);

        manager.addNewTask(taskOld);
        assertSame(taskOld, manager.getTask(taskOld.getId()).get(), "Задача должна добавиться");

        manager.updateTask(taskNew);
        assertSame(taskOld, manager.getTask(taskOld.getId()).get(), "Задача должна остаться прежней");
        assertEquals(Optional.empty(), manager.getTask(taskNew.getId()), "Новой версии не должно быть в менеджере");
    }

    @Test
    public void updateEpic_shouldUpdateEpic_epicWithIdExists() {
        Epic epicOld = new Epic(1, "Test Old", "Testing epic Old");
        Epic epicNew = new Epic(1, "Test New", "Testing epic New");

        manager.addNewEpic(epicOld);
        assertSame(epicOld, manager.getEpic(epicOld.getId()).get(), "Эпик должен добавиться");

        manager.updateEpic(epicNew);
        assertEquals(epicOld.getName(), epicNew.getName(), "Имя эпика должно обновиться");
        assertEquals(epicOld.getDescription(), epicNew.getDescription(), "Описание эпика должно обновиться");
        assertSame(epicOld, manager.getEpic(epicOld.getId()).get(), "Эпик должен остаться тем же объектом в менеджере");
    }

    @Test
    public void updateEpic_shouldNotUpdateEpic_epicWithIdDoesNotExist() {
        Epic epicOld = new Epic(1, "Test Old", "Testing epic Old");
        Epic epicNew = new Epic(2, "Test New", "Testing epic New");

        manager.addNewEpic(epicOld);
        assertSame(epicOld, manager.getEpic(epicOld.getId()).get(), "Эпик должен добавиться");

        manager.updateEpic(epicNew);
        assertNotEquals(epicOld.getName(), epicNew.getName(), "Имя эпика должно остаться прежним");
        assertNotEquals(epicOld.getDescription(), epicNew.getDescription(), "Описание эпика должно остаться прежним");
        assertSame(epicOld, manager.getEpic(epicOld.getId()).get(), "Эпик должен остаться тем же объектом в менеджере");
    }

    @Test
    public void updateSubtask_shouldUpdateSubtask_subtaskWithIdExists() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtaskOld = new Subtask(1, "Test Old", "Testing subtask Old", TaskStatus.NEW, 3);
        Subtask subtaskNew = new Subtask(1, "Test New", "Testing subtask New", TaskStatus.NEW, 3);

        manager.addNewSubtask(subtaskOld);
        assertSame(subtaskOld, manager.getSubtask(subtaskOld.getId()).get(), "Подзадача должна добавиться");

        manager.updateSubtask(subtaskNew);
        assertSame(subtaskNew, manager.getSubtask(subtaskOld.getId()).get(), "Подзадача должна обновиться");
    }

    @Test
    public void updateSubtask_shouldNotUpdateSubtask_subtaskWithIdDoesNotExist() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtaskOld = new Subtask(1, "Test Old", "Testing subtask Old", TaskStatus.NEW, 3);
        Subtask subtaskNew = new Subtask(2, "Test New", "Testing subtask New", TaskStatus.NEW, 3);

        manager.addNewSubtask(subtaskOld);
        assertSame(subtaskOld, manager.getSubtask(subtaskOld.getId()).get(), "Подзадача должна добавиться");

        manager.updateSubtask(subtaskNew);
        assertSame(subtaskOld, manager.getSubtask(subtaskOld.getId()).get(), "Подзадача должна остаться прежней");
    }

    @Test
    public void updateSubtask_shouldNotUpdateSubtask_epicWithIdDoesNotExist() {
        Epic epic = new Epic(3, "Test 1", "Testing epic for subtasks");
        manager.addNewEpic(epic);

        Subtask subtaskOld = new Subtask(1, "Test Old", "Testing subtask Old", TaskStatus.NEW, 3);
        Subtask subtaskNew = new Subtask(1, "Test New", "Testing subtask New", TaskStatus.NEW, 4);

        manager.addNewSubtask(subtaskOld);
        assertSame(subtaskOld, manager.getSubtask(subtaskOld.getId()).get(), "Подзадача должна добавиться");

        manager.updateSubtask(subtaskNew);
        assertSame(subtaskOld, manager.getSubtask(subtaskOld.getId()).get(), "Подзадача должна остаться прежней");
    }

    @Test
    public void deleteTask_shouldDeleteTask_taskExists() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        int id = manager.addNewTask(task);

        assertSame(task, manager.getTask(id).get(), "Задача должна добавиться в менеджер");

        manager.deleteTask(id);
        assertEquals(0, manager.getTasks().size(), "Список задач должен быть пуст");
    }

    @Test
    public void deleteTask_shouldNotDeleteTask_taskDoesNotExist() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        int id = manager.addNewTask(task);

        assertSame(task, manager.getTask(id).get(), "Задача должна добавиться в менеджер");

        manager.deleteTask(1000);
        assertEquals(1, manager.getTasks().size(), "Список задач должен иметь одну задачу");
    }

    @Test
    public void deleteEpic_shouldDeleteEpic_epicExists() {
        Epic epic = new Epic(1, "Epic 1", "Testing task 1");
        int id = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(2, "Test 1", "Testing subtask 1", TaskStatus.NEW, id);
        Subtask subtask2 = new Subtask(3, "Test 2", "Testing subtask 2", TaskStatus.NEW, id);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(epic, manager.getEpic(id).get(), "Эпик должен добавиться в менеджер");
        assertEquals(2, manager.getEpicSubtasks(id).size(), "В списке подзадач эпика должно быть 2 подзадачи");

        manager.deleteEpic(id);
        assertEquals(0, manager.getEpics().size(), "Список эпиков должен быть пуст");
        assertEquals(0, manager.getSubtasks().size(), "Список подзадач должен быть пуст");
    }

    @Test
    public void deleteEpic_shouldNotDeleteEpic_epicDoesNotExist() {
        Epic epic = new Epic(1, "Epic 1", "Testing task 1");
        int id = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(2, "Test 1", "Testing subtask 1", TaskStatus.NEW, id);
        Subtask subtask2 = new Subtask(3, "Test 2", "Testing subtask 2", TaskStatus.NEW, id);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertSame(epic, manager.getEpic(id).get(), "Эпик должен добавиться в менеджер");

        manager.deleteEpic(1000);
        assertEquals(1, manager.getEpics().size(), "Список эпиков должен иметь один эпик");
        assertEquals(2, manager.getSubtasks().size(), "В списке подзадач должно быть две подзадачи");
    }

    @Test
    public void deleteSubtask_shouldDeleteSubtask_subtaskExists() {
        Epic epic = new Epic(1, "Epic 1", "Testing task 1");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(2, "Test 1", "Testing subtask 1", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask(3, "Test 2", "Testing subtask 2", TaskStatus.NEW, epicId);
        manager.addNewSubtask(subtask1);
        int id = manager.addNewSubtask(subtask2);

        assertSame(epic, manager.getEpic(epicId).get(), "Эпик должен добавиться в менеджер");
        assertEquals(2, manager.getEpicSubtasks(epicId).size(), "В списке подзадач эпика должно быть 2 подзадачи");

        manager.deleteSubtask(id);
        assertEquals(1, manager.getEpics().size(), "Эпик должен остаться");
        assertEquals(1, manager.getSubtasks().size(), "Должна быть одна подзадача");
    }

    @Test
    public void deleteSubtask_shouldNotDeleteSubtask_subtaskDoesNotExist() {
        Epic epic = new Epic(1, "Epic 1", "Testing task 1");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(2, "Test 1", "Testing subtask 1", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask(3, "Test 2", "Testing subtask 2", TaskStatus.NEW, epicId);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertSame(epic, manager.getEpic(epicId).get(), "Эпик должен добавиться в менеджер");
        assertEquals(2, manager.getEpicSubtasks(epicId).size(), "В списке подзадач эпика должно быть 2 подзадачи");

        manager.deleteSubtask(1000);
        assertEquals(1, manager.getEpics().size(), "Эпик должен остаться");
        assertEquals(2, manager.getSubtasks().size(), "В списке подзадач должно быть 2 подзадачи");
    }

    @Test
    public void deleteTasks_shouldDeleteAllTasks() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);

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

        Subtask subtask1 = new Subtask(1, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(2, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic.getId());
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
        Subtask subtask1 = new Subtask(3, "Test 1", "Testing subtask 1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(4, "Test 2", "Testing subtask 2", TaskStatus.NEW, epic1.getId());
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(2, manager.getEpics().size(), "В менеджере должно быть 2 эпика");

        manager.deleteEpics();

        assertEquals(0, manager.getEpics().size(), "В менеджере не должно быть эпиков");
        assertEquals(0, manager.getSubtasks().size(), "В менеджере не должно быть подзадач");
    }

    @Test
    public void getHistory_shouldReturnViewsHistory() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW);

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

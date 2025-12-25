package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.exceptions.InvalidTaskCompletionTime;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return Managers.getDefault();
    }

    @Test
    public void getEndTime_shouldCalcEndTime_ifStartTimeAndDurationAreProvided() {
        LocalDateTime start = LocalDateTime.of(2020, 1, 1, 15, 0, 0);
        Duration duration = Duration.ofMinutes(60);
        LocalDateTime end = start.plus(duration);

        Task task = new Task(1, "Task 1", "Testing task 1", TaskStatus.NEW, duration, start);
        Epic epic = new Epic(2, "Epic 1", "Testing epic 1");
        Subtask subtask = new Subtask(3, "Sub 1", "Testing subtask 1", TaskStatus.NEW, 2, duration, start.plusYears(1));

        manager.addNewTask(task);
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        assertEquals(end, task.getEndTime().get(), "Время завершения должно быть 01.01.2020 16:00");
        assertEquals(end.plusYears(1), subtask.getEndTime().get(), "Время завершения должно быть 01.01.2020 16:00");
        assertEquals(end.plusYears(1), epic.getEndTime().get(), "Время завершения должно быть 01.01.2020 16:00");
    }

    @Test
    public void getEndTime_endTimeOfEpicShouldRemainAndStartTimeShouldRecalc_ifNewSubtaskStartsEarlier() {
        Duration duration = Duration.ofMinutes(60);

        LocalDateTime start1 = LocalDateTime.of(2020, 1, 1, 15, 0, 0);
        LocalDateTime end1 = start1.plus(duration);

        LocalDateTime start2 = LocalDateTime.of(2019, 1, 1, 15, 0, 0);
        LocalDateTime end2 = start2.plus(duration);

        Epic epic = new Epic(2, "Epic 1", "Testing epic 1");
        Subtask subtask1 = new Subtask(3, "Sub 1", "Testing subtask 1", TaskStatus.NEW, 2, duration, start1);
        Subtask subtask2 = new Subtask(4, "Sub 2", "Testing subtask 2", TaskStatus.NEW, 2, duration, start2);

        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);

        assertEquals(end1, subtask1.getEndTime().get(), "Время завершения задачи должно быть 01.01.2020 16:00");
        assertEquals(end2, subtask2.getEndTime().get(), "Время завершения подзадачи должно быть 01.01.2020 16:00");
        assertEquals(end1, epic.getEndTime().get(), "Время завершения эпика должно быть 01.01.2020 16:00");

        manager.addNewSubtask(subtask2);

        assertEquals(start2, epic.getStartTime().get(), "Время начала эпика должно быть 01.01.2019 15:00");
        assertEquals(end1, epic.getEndTime().get(), "Время завершения эпика должно быть 01.01.2020 16:00");
    }

    @Test
    public void getEndTime_endTimeOfEpicShouldRecalcAndStartTimeShouldRemain_ifNewSubtaskEndsLater() {
        Duration duration = Duration.ofMinutes(60);

        LocalDateTime start1 = LocalDateTime.of(2020, 1, 1, 15, 0, 0);
        LocalDateTime end1 = start1.plus(duration);

        LocalDateTime start2 = LocalDateTime.of(2021, 1, 1, 15, 0, 0);
        LocalDateTime end2 = start2.plus(duration);

        Epic epic = new Epic(2, "Epic 1", "Testing epic 1");
        Subtask subtask1 = new Subtask(3, "Sub 1", "Testing subtask 1", TaskStatus.NEW, 2, duration, start1);
        Subtask subtask2 = new Subtask(4, "Sub 2", "Testing subtask 2", TaskStatus.NEW, 2, duration, start2);

        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);

        assertEquals(end1, subtask1.getEndTime().get(), "Время завершения задачи должно быть 01.01.2020 16:00");
        assertEquals(end2, subtask2.getEndTime().get(), "Время завершения подзадачи должно быть 01.01.2021 16:00");
        assertEquals(end1, epic.getEndTime().get(), "Время завершения эпика должно быть 01.01.2020 16:00");

        manager.addNewSubtask(subtask2);

        assertEquals(start1, epic.getStartTime().get(), "Время начала эпика должно быть 01.01.2020 15:00");
        assertEquals(end2, epic.getEndTime().get(), "Время завершения эпика должно быть 01.01.2021 16:00");
    }

    @Test
    public void addNewTask_shouldThrowException_ifTasksIntersectByTime() {
        LocalDateTime start = LocalDateTime.of(2020, 1, 1, 15, 0, 0);
        Duration duration = Duration.ofMinutes(60);
        LocalDateTime end = start.plus(duration);

        Task task1 = new Task(1, "Task 1", "Testing task 1", TaskStatus.NEW, duration, start);
        Task task2 = new Task(2, "Task 2", "Testing task 2", TaskStatus.DONE, duration, start.minusMinutes(30));

        manager.addNewTask(task1);

        InvalidTaskCompletionTime exception = assertThrows(
                InvalidTaskCompletionTime.class,
                () -> manager.addNewTask(task2)
        );
        assertEquals("Задача пересекается по времени с другими задачами", exception.getMessage());
    }

    @Test
    public void addNewSubtask_shouldThrowException_ifTasksIntersectByTime() {
        LocalDateTime start = LocalDateTime.of(2020, 1, 1, 15, 0, 0);
        Duration duration = Duration.ofMinutes(60);

        Epic epic = new Epic(2, "Epic 1", "Testing epic 1");
        Subtask subtask1 = new Subtask(3, "Sub 1", "Testing subtask 1", TaskStatus.NEW, 2, duration, start);
        Subtask subtask2 = new Subtask(4, "Sub 2", "Testing subtask 2", TaskStatus.NEW, 2, duration, start.minusMinutes(30));

        manager.addNewEpic(epic);
        manager.addNewTask(subtask1);

        InvalidTaskCompletionTime exception = assertThrows(
                InvalidTaskCompletionTime.class,
                () -> manager.addNewSubtask(subtask2)
        );
        assertEquals("Подзадача пересекается по времени с другими задачами", exception.getMessage());
    }

    @Test
    public void updateEpicStatus_shouldBeNew_allSubtasksNew() {
        Epic epic = new Epic(1, "Epic 1", "Testing epic 1");
        Subtask subtask1 = new Subtask(2, "Sub 1", "Testing subtask 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask(3, "Sub 2", "Testing subtask 2", TaskStatus.NEW, 1);

        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    public void updateEpicStatus_shouldBeDone_allSubtasksDone() {
        Epic epic = new Epic(1, "Epic 1", "Testing epic 1");
        Subtask subtask1 = new Subtask(2, "Sub 1", "Testing subtask 1", TaskStatus.DONE, 1);
        Subtask subtask2 = new Subtask(3, "Sub 2", "Testing subtask 2", TaskStatus.DONE, 1);

        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    public void updateEpicStatus_shouldBeInProgress_oneSubtaskNewRestSubtasksDone() {
        Epic epic = new Epic(1, "Epic 1", "Testing epic 1");
        Subtask subtask1 = new Subtask(2, "Sub 1", "Testing subtask 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask(3, "Sub 2", "Testing subtask 2", TaskStatus.DONE, 1);

        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    public void updateEpicStatus_shouldBeInProgress_allInProgress() {
        Epic epic = new Epic(1, "Epic 1", "Testing epic 1");
        Subtask subtask1 = new Subtask(2, "Sub 1", "Testing subtask 1", TaskStatus.IN_PROGRESS, 1);
        Subtask subtask2 = new Subtask(3, "Sub 2", "Testing subtask 2", TaskStatus.IN_PROGRESS, 1);

        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }
}

package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void testEqualityById(){
        Subtask s0 = new Subtask(1, "Test 1", "Testing task 1", TaskStatus.NEW, 3);
        Subtask s1 = new Subtask(1, "Test 2", "Testing task 2", TaskStatus.IN_PROGRESS, 3);
        assertEquals(s0, s1, "task entities should be compared by id");
    }

    @Test
    void testConstructorThrowsExceptionWhenEpicIdEqualsId() {
        int id = 1;
        int epicId = 1; // Same as id, should throw
        assertThrows(
                InvalidEpicIdException.class,
                () -> new Subtask(id, "name", "desc", TaskStatus.NEW, epicId),
                "EpicID should not be equal to subtaskID"
        );
    }

    @Test
    void testConstructorSucceedsWhenEpicIdNotEqualsId() {
        int id = 1;
        int epicId = 2; // Different from id, should not throw
        Subtask subtask = new Subtask(id, "name", "desc", TaskStatus.NEW, epicId);
        assertEquals(epicId, subtask.getEpicId());
        assertEquals(id, subtask.getId());
        assertEquals("name", subtask.getName());
        assertEquals("desc", subtask.getDescription());
        assertEquals(TaskStatus.NEW, subtask.getStatus());
    }
}

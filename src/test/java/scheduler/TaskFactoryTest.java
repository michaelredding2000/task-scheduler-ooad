package scheduler;

import org.junit.jupiter.api.Test;
import scheduler.factory.TaskFactory;
import scheduler.model.Task;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Factory pattern (TaskFactory).
 *
 * Verifies that each factory method produces a task with the correct
 * priority and initial state, and that invalid inputs are rejected.
 */
class TaskFactoryTest {

    @Test
    void highPriorityTaskHasCorrectPriority() {
        Task task = TaskFactory.createHighPriority("urgent-job");
        assertEquals(TaskFactory.PRIORITY_HIGH, task.getPriority());
    }

    @Test
    void normalPriorityTaskHasCorrectPriority() {
        Task task = TaskFactory.createNormal("routine-job");
        assertEquals(TaskFactory.PRIORITY_NORMAL, task.getPriority());
    }

    @Test
    void lowPriorityTaskHasCorrectPriority() {
        Task task = TaskFactory.createLowPriority("background-job");
        assertEquals(TaskFactory.PRIORITY_LOW, task.getPriority());
    }

    @Test
    void customPriorityIsPreserved() {
        Task task = TaskFactory.createWithPriority("custom-job", 7);
        assertEquals(7, task.getPriority());
    }

    @Test
    void newTaskStartsInPendingState() {
        Task task = TaskFactory.createNormal("check-state");
        assertEquals(Task.State.PENDING, task.getState());
    }

    @Test
    void taskNameIsPreserved() {
        Task task = TaskFactory.createNormal("my-task");
        assertEquals("my-task", task.getName());
    }

    @Test
    void blankNameThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> TaskFactory.createNormal("   "));
    }

    @Test
    void nullNameThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> TaskFactory.createNormal(null));
    }
}

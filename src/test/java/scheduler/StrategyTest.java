package scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scheduler.factory.TaskFactory;
import scheduler.model.Task;
import scheduler.singleton.Scheduler;
import scheduler.strategy.FifoStrategy;
import scheduler.strategy.PriorityStrategy;
import scheduler.strategy.RoundRobinStrategy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Strategy pattern.
 *
 * Verifies that each scheduling algorithm selects tasks in the correct
 * order and that the strategy can be swapped without restarting the scheduler.
 */
class StrategyTest {

    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = Scheduler.getInstance();
    }

    // -----------------------------------------------------------------------
    // FIFO Strategy
    // -----------------------------------------------------------------------

    @Test
    void fifoRunsTasksInSubmissionOrder() {
        scheduler.reset(new FifoStrategy());
        // Submit in this order: A, B, C
        scheduler.submit(TaskFactory.createNormal("A"));
        // Sleep 1ms so createdAt timestamps are distinct
        sleep(2);
        scheduler.submit(TaskFactory.createNormal("B"));
        sleep(2);
        scheduler.submit(TaskFactory.createNormal("C"));

        List<Task> executed = scheduler.runAll();

        assertEquals(3, executed.size());
        assertEquals("A", executed.get(0).getName());
        assertEquals("B", executed.get(1).getName());
        assertEquals("C", executed.get(2).getName());
    }

    @Test
    void fifoIgnoresPriority() {
        scheduler.reset(new FifoStrategy());
        scheduler.submit(TaskFactory.createLowPriority("low-first"));
        sleep(2);
        scheduler.submit(TaskFactory.createHighPriority("high-second"));

        List<Task> executed = scheduler.runAll();

        // FIFO: "low-first" was submitted first, so it runs first
        assertEquals("low-first", executed.get(0).getName());
    }

    // -----------------------------------------------------------------------
    // Priority Strategy
    // -----------------------------------------------------------------------

    @Test
    void priorityRunsHighestPriorityFirst() {
        scheduler.reset(new PriorityStrategy());
        scheduler.submit(TaskFactory.createLowPriority("low"));
        scheduler.submit(TaskFactory.createHighPriority("high"));
        scheduler.submit(TaskFactory.createNormal("normal"));

        List<Task> executed = scheduler.runAll();

        assertEquals("high",   executed.get(0).getName());
        assertEquals("normal", executed.get(1).getName());
        assertEquals("low",    executed.get(2).getName());
    }

    @Test
    void priorityTieBreaksBySubmissionOrder() {
        scheduler.reset(new PriorityStrategy());
        // Both have the same priority -- earlier submission should win
        scheduler.submit(TaskFactory.createNormal("first"));
        sleep(2);
        scheduler.submit(TaskFactory.createNormal("second"));

        Task chosen = scheduler.runNext();
        // "first" was submitted earlier, so it should be chosen
        assertEquals("first", chosen.getName());
    }

    // -----------------------------------------------------------------------
    // Round-Robin Strategy
    // -----------------------------------------------------------------------

    @Test
    void roundRobinServesAllTasks() {
        RoundRobinStrategy rr = new RoundRobinStrategy();
        rr.reset();
        scheduler.reset(rr);
        scheduler.submit(TaskFactory.createNormal("x"));
        scheduler.submit(TaskFactory.createNormal("y"));
        scheduler.submit(TaskFactory.createNormal("z"));

        List<Task> executed = scheduler.runAll();

        // All three tasks must have been executed
        assertEquals(3, executed.size());
    }

    // -----------------------------------------------------------------------
    // Strategy swap at runtime
    // -----------------------------------------------------------------------

    @Test
    void strategyCanBeSwappedWithoutLosingQueue() {
        scheduler.reset(new FifoStrategy());
        scheduler.submit(TaskFactory.createNormal("task1"));
        scheduler.submit(TaskFactory.createHighPriority("task2"));

        // Swap strategy mid-flight
        scheduler.setStrategy(new PriorityStrategy());

        // task2 (high priority) should now be selected first
        Task first = scheduler.runNext();
        assertEquals("task2", first.getName());
    }

    @Test
    void emptyQueueReturnsNull() {
        scheduler.reset(new FifoStrategy());
        assertNull(scheduler.runNext());
    }

    @Test
    void noStrategyThrowsIllegalState() {
        // Simulate a misconfigured scheduler (strategy never set after reset)
        scheduler.reset(null);
        scheduler.submit(TaskFactory.createNormal("orphan"));
        assertThrows(IllegalStateException.class, () -> scheduler.runNext());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

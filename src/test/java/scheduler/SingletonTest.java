package scheduler;

import org.junit.jupiter.api.Test;
import scheduler.singleton.Scheduler;
import scheduler.strategy.FifoStrategy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Singleton pattern.
 *
 * Verifies that Scheduler.getInstance() always returns the same object
 * reference, and that resetting state does not create a new instance.
 */
class SingletonTest {

    @Test
    void sameInstanceReturnedEveryTime() {
        Scheduler a = Scheduler.getInstance();
        Scheduler b = Scheduler.getInstance();
        assertSame(a, b, "getInstance() should return the same object on every call");
    }

    @Test
    void instanceIsNotNull() {
        assertNotNull(Scheduler.getInstance());
    }

    @Test
    void resetClearsQueueButKeepsSameInstance() {
        Scheduler s = Scheduler.getInstance();
        s.reset(new FifoStrategy());

        // Submit a task then reset
        s.submit(scheduler.factory.TaskFactory.createNormal("to-be-cleared"));
        assertEquals(1, s.getPendingCount());

        s.reset(new FifoStrategy());
        assertEquals(0, s.getPendingCount());

        // Instance should still be the same object
        assertSame(s, Scheduler.getInstance());
    }

    @Test
    void multipleReferencesShareState() {
        Scheduler ref1 = Scheduler.getInstance();
        Scheduler ref2 = Scheduler.getInstance();

        ref1.reset(new FifoStrategy());
        ref1.submit(scheduler.factory.TaskFactory.createNormal("shared-task"));

        // ref2 should see the task submitted through ref1
        assertEquals(1, ref2.getPendingCount());
    }
}

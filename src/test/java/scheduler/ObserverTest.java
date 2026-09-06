package scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scheduler.factory.TaskFactory;
import scheduler.observer.MetricsCollector;
import scheduler.observer.TaskEvent;
import scheduler.singleton.Scheduler;
import scheduler.strategy.FifoStrategy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Observer pattern.
 *
 * Uses MetricsCollector as a test-friendly observer (it counts events
 * rather than printing them).  Verifies that:
 * - All observers receive every event.
 * - Event counts match the number of tasks processed.
 * - Removed observers stop receiving events.
 */
class ObserverTest {

    private Scheduler scheduler;
    private MetricsCollector metrics;

    @BeforeEach
    void setUp() {
        // Reset the singleton so each test starts with a clean slate
        scheduler = Scheduler.getInstance();
        metrics = new MetricsCollector();
        scheduler.reset(new FifoStrategy());
        scheduler.addObserver(metrics);
    }

    @Test
    void submittingTaskFiresSubmittedEvent() {
        scheduler.submit(TaskFactory.createNormal("task-a"));
        assertEquals(1, metrics.getCount(TaskEvent.Type.SUBMITTED));
    }

    @Test
    void runningTaskFiresStartedAndCompletedEvents() {
        scheduler.submit(TaskFactory.createNormal("task-b"));
        scheduler.runNext();
        assertEquals(1, metrics.getCount(TaskEvent.Type.STARTED));
        assertEquals(1, metrics.getCount(TaskEvent.Type.COMPLETED));
    }

    @Test
    void threeTasksFireSixEventsTotal() {
        // Each task fires: SUBMITTED, STARTED, COMPLETED = 3 events
        // Three tasks = 9 events total
        scheduler.submit(TaskFactory.createNormal("t1"));
        scheduler.submit(TaskFactory.createNormal("t2"));
        scheduler.submit(TaskFactory.createNormal("t3"));
        scheduler.runAll();
        assertEquals(9, metrics.getTotalCount());
    }

    @Test
    void removedObserverReceivesNoMoreEvents() {
        scheduler.removeObserver(metrics);
        scheduler.submit(TaskFactory.createNormal("task-c"));
        scheduler.runNext();
        // metrics was removed before any events -- should see nothing
        assertEquals(0, metrics.getTotalCount());
    }

    @Test
    void multipleObserversEachReceiveAllEvents() {
        MetricsCollector metrics2 = new MetricsCollector();
        scheduler.addObserver(metrics2);

        scheduler.submit(TaskFactory.createNormal("task-d"));
        scheduler.runNext();

        // Both collectors should have seen the same events
        assertEquals(metrics.getTotalCount(), metrics2.getTotalCount());
        assertEquals(3, metrics.getTotalCount()); // SUBMITTED + STARTED + COMPLETED
    }
}

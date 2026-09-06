package scheduler.strategy;

import scheduler.model.Task;
import java.util.List;

/**
 * Strategy Pattern -- defines the contract for a scheduling algorithm.
 *
 * <h2>Why Strategy here?</h2>
 * <p>Different workloads need different scheduling disciplines.  A web server
 * might want priority scheduling (high-priority requests first).  A batch
 * job runner might want FIFO (process in submission order, no starvation).
 * A real-time system might want something else entirely.
 *
 * <p>Without Strategy, you'd have a large {@code switch} or chain of
 * {@code if-else} blocks inside the Scheduler that grows every time a new
 * algorithm is needed.  With Strategy, each algorithm is an independent class
 * that can be swapped at runtime without touching the Scheduler.
 *
 * <h2>Interface contract</h2>
 * <p>{@code selectNext()} must return the task that should run next, or
 * {@code null} if the queue is empty.  Implementations must NOT modify the
 * list -- that's the Scheduler's job.
 */
public interface SchedulingStrategy {

    /**
     * Select the next task to execute from the pending queue.
     *
     * @param pendingTasks an unmodifiable view of all PENDING tasks
     * @return the task to run next, or {@code null} if the list is empty
     */
    Task selectNext(List<Task> pendingTasks);

    /** Human-readable name used in logs and reports. */
    String getName();
}

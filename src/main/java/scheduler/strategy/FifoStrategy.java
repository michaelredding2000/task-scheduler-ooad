package scheduler.strategy;

import scheduler.model.Task;
import java.util.Comparator;
import java.util.List;

/**
 * First-In, First-Out scheduling.
 *
 * <p>Selects the task with the earliest {@code createdAt} timestamp.
 * This guarantees that every task is eventually scheduled (no starvation),
 * but a high-priority task submitted late will wait behind all earlier tasks.
 *
 * <p>FIFO is a good default for batch jobs where fairness matters more
 * than urgency.
 */
public class FifoStrategy implements SchedulingStrategy {

    @Override
    public Task selectNext(List<Task> pendingTasks) {
        if (pendingTasks.isEmpty()) return null;
        // Return the task submitted earliest
        return pendingTasks.stream()
                .min(Comparator.comparing(Task::getCreatedAt))
                .orElse(null);
    }

    @Override
    public String getName() { return "FIFO"; }
}

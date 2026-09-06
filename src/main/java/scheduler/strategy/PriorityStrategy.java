package scheduler.strategy;

import scheduler.model.Task;
import java.util.Comparator;
import java.util.List;

/**
 * Priority-based scheduling.
 *
 * <p>Selects the task with the highest {@code priority} value.  Ties are
 * broken by submission time (earlier task wins) to prevent two equal-priority
 * tasks from starving each other.
 *
 * <p>Priority scheduling is appropriate when urgency varies significantly
 * across tasks (e.g. user-facing vs. background work).  The trade-off is that
 * low-priority tasks may wait indefinitely if high-priority work keeps
 * arriving -- a problem called starvation.
 */
public class PriorityStrategy implements SchedulingStrategy {

    @Override
    public Task selectNext(List<Task> pendingTasks) {
        if (pendingTasks.isEmpty()) return null;
        return pendingTasks.stream()
                // Highest priority first; break ties by earliest submission
                .max(Comparator.comparingInt(Task::getPriority)
                        .thenComparing(Comparator.comparing(Task::getCreatedAt).reversed()))
                .orElse(null);
    }

    @Override
    public String getName() { return "Priority"; }
}

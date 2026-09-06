package scheduler.strategy;

import scheduler.model.Task;
import java.util.Comparator;
import java.util.List;

/**
 * Round-Robin scheduling.
 *
 * <p>Each call to {@code selectNext()} advances a position counter and picks
 * the task at that position (sorted by submission time for determinism).
 * This gives every task an equal share of scheduler attention over time,
 * regardless of priority.
 *
 * <p>Round-Robin is fair and starvation-free.  It is commonly used in OS
 * process scheduling and load-balancing.  The trade-off versus priority
 * scheduling is that urgent tasks may have to wait their turn.
 *
 * <p>Note: position is stored in the strategy instance, so using the same
 * strategy instance across multiple Scheduler instances would share state.
 * For this project one Scheduler = one strategy instance, so this is safe.
 */
public class RoundRobinStrategy implements SchedulingStrategy {

    // Index into the sorted pending list; wraps around when tasks are removed
    private int position = 0;

    @Override
    public Task selectNext(List<Task> pendingTasks) {
        if (pendingTasks.isEmpty()) return null;

        // Sort by submission time for a stable, deterministic ordering
        List<Task> sorted = pendingTasks.stream()
                .sorted(Comparator.comparing(Task::getCreatedAt))
                .toList();

        // Clamp position in case tasks were removed since last call
        position = position % sorted.size();
        Task chosen = sorted.get(position);

        // Advance position for the next call (will be re-clamped if needed)
        position = (position + 1) % Math.max(sorted.size() - 1, 1);

        return chosen;
    }

    @Override
    public String getName() { return "Round-Robin"; }

    /** Reset the round-robin pointer (useful between test cases). */
    public void reset() { position = 0; }
}

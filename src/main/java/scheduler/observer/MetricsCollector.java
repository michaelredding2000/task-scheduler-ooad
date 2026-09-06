package scheduler.observer;

import java.util.EnumMap;
import java.util.Map;

/**
 * A TaskObserver that counts events by type.
 *
 * <p>In a production system this might push metrics to Prometheus or
 * CloudWatch.  Here it stores counts in a map so unit tests can assert
 * that the correct number of events were fired.
 */
public class MetricsCollector implements TaskObserver {

    // Counts keyed by event type
    private final Map<TaskEvent.Type, Integer> counts =
            new EnumMap<>(TaskEvent.Type.class);

    @Override
    public void onTaskEvent(TaskEvent event) {
        counts.merge(event.getType(), 1, Integer::sum);
    }

    /**
     * Return the number of events fired for the given type.
     * Returns 0 if no events of that type have been fired yet.
     */
    public int getCount(TaskEvent.Type type) {
        return counts.getOrDefault(type, 0);
    }

    /** Return the total number of events received across all types. */
    public int getTotalCount() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Reset all counters. */
    public void reset() {
        counts.clear();
    }
}

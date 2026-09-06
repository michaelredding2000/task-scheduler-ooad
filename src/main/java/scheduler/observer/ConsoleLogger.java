package scheduler.observer;

import java.time.Instant;

/**
 * A TaskObserver that prints every state-change event to stdout.
 *
 * <p>Demonstrates how a subscriber can filter events it cares about.
 * This implementation logs everything; a real logger might filter by
 * event type or task priority.
 */
public class ConsoleLogger implements TaskObserver {

    @Override
    public void onTaskEvent(TaskEvent event) {
        System.out.printf("[LOG  %s]  %-10s  %s%n",
                Instant.now(), event.getType(), event.getTask());
    }
}

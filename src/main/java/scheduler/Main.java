package scheduler;

import scheduler.factory.TaskFactory;
import scheduler.model.Task;
import scheduler.observer.ConsoleLogger;
import scheduler.observer.MetricsCollector;
import scheduler.observer.TaskEvent;
import scheduler.singleton.Scheduler;
import scheduler.strategy.FifoStrategy;
import scheduler.strategy.PriorityStrategy;

/**
 * Demonstrates the scheduler with two runs:
 *  1. FIFO strategy   -- tasks execute in submission order
 *  2. Priority strategy -- tasks execute highest-priority first
 *
 * Run with: java -cp target/classes scheduler.Main
 */
public class Main {

    public static void main(String[] args) {
        Scheduler scheduler = Scheduler.getInstance();
        MetricsCollector metrics = new MetricsCollector();

        scheduler.addObserver(new ConsoleLogger());
        scheduler.addObserver(metrics);

        // ----------------------------------------------------------------
        // Run 1: FIFO
        // ----------------------------------------------------------------
        System.out.println("\n=== FIFO Strategy ===");
        scheduler.reset(new FifoStrategy());
        scheduler.addObserver(new ConsoleLogger());
        scheduler.addObserver(metrics);

        // Submit in order: low -> high -> normal
        scheduler.submit(TaskFactory.createLowPriority("cleanup-logs"));
        scheduler.submit(TaskFactory.createHighPriority("handle-payment"));
        scheduler.submit(TaskFactory.createNormal("send-email"));

        scheduler.runAll();

        System.out.printf("%nMetrics after FIFO run: submitted=%d, completed=%d%n",
                metrics.getCount(TaskEvent.Type.SUBMITTED),
                metrics.getCount(TaskEvent.Type.COMPLETED));

        // ----------------------------------------------------------------
        // Run 2: Priority
        // ----------------------------------------------------------------
        System.out.println("\n=== Priority Strategy ===");
        scheduler.reset(new PriorityStrategy());
        scheduler.addObserver(new ConsoleLogger());
        metrics.reset();
        scheduler.addObserver(metrics);

        scheduler.submit(TaskFactory.createLowPriority("cleanup-logs"));
        scheduler.submit(TaskFactory.createHighPriority("handle-payment"));
        scheduler.submit(TaskFactory.createNormal("send-email"));

        System.out.println();
        System.out.println("Expected execution order: handle-payment, send-email, cleanup-logs");

        for (Task t : scheduler.runAll()) {
            System.out.println("  Executed: " + t.getName() + " (priority=" + t.getPriority() + ")");
        }

        System.out.printf("%nMetrics after Priority run: submitted=%d, completed=%d%n",
                metrics.getCount(TaskEvent.Type.SUBMITTED),
                metrics.getCount(TaskEvent.Type.COMPLETED));
    }
}

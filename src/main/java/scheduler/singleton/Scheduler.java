package scheduler.singleton;

import scheduler.model.Task;
import scheduler.observer.TaskEvent;
import scheduler.observer.TaskObserver;
import scheduler.strategy.SchedulingStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Pattern -- the shared, system-wide task scheduler.
 *
 * <h2>Why Singleton here?</h2>
 * <p>A task scheduler is a shared resource.  Every part of the application
 * that submits or runs tasks must see the same queue.  If two independent
 * Scheduler instances existed, they would each maintain separate queues and
 * observers would miss events fired by the other instance.
 *
 * <p>The Singleton guarantees a single, shared state throughout the JVM
 * lifetime.  This is the textbook use case: a resource manager that should
 * have exactly one instance.
 *
 * <h2>Implementation note</h2>
 * <p>We use the "initialisation-on-demand holder" idiom instead of a
 * double-checked lock.  The JVM's class-loading guarantee makes the holder
 * class's static field thread-safe without any explicit synchronisation.
 * See: Bloch, <em>Effective Java</em>, Item 83.
 *
 * <h2>Testing note</h2>
 * <p>Singletons are notoriously hard to test in isolation because state
 * persists between tests.  We expose {@link #reset(SchedulingStrategy)} to
 * let tests clear the queue and swap the strategy between test cases.
 */
public class Scheduler {

    // -----------------------------------------------------------------------
    // Singleton -- initialisation-on-demand holder idiom
    // -----------------------------------------------------------------------

    /** Loaded lazily and thread-safely by the JVM's class loader. */
    private static class Holder {
        static final Scheduler INSTANCE = new Scheduler();
    }

    /** Return the single shared Scheduler instance. */
    public static Scheduler getInstance() {
        return Holder.INSTANCE;
    }

    // -----------------------------------------------------------------------
    // Instance fields
    // -----------------------------------------------------------------------

    /** All tasks waiting to be scheduled (state == PENDING). */
    private final List<Task> pendingQueue = new ArrayList<>();

    /** Observers notified on every state change. */
    private final List<TaskObserver> observers = new ArrayList<>();

    /** The active scheduling algorithm (swappable at runtime). */
    private SchedulingStrategy strategy;

    // -----------------------------------------------------------------------
    // Constructor -- private so no code outside this class can call `new`
    // -----------------------------------------------------------------------

    private Scheduler() {}

    // -----------------------------------------------------------------------
    // Strategy management
    // -----------------------------------------------------------------------

    /**
     * Set the scheduling algorithm.  Can be called at any time to change
     * behaviour without restarting the scheduler or losing the queue.
     */
    public void setStrategy(SchedulingStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("Strategy must not be null");
        this.strategy = strategy;
        System.out.println("[Scheduler] Strategy set to: " + strategy.getName());
    }

    public SchedulingStrategy getStrategy() { return strategy; }

    // -----------------------------------------------------------------------
    // Observer management (Observer pattern)
    // -----------------------------------------------------------------------

    /** Register an observer to receive task lifecycle events. */
    public void addObserver(TaskObserver observer) {
        observers.add(observer);
    }

    /** Deregister an observer.  No-op if not registered. */
    public void removeObserver(TaskObserver observer) {
        observers.remove(observer);
    }

    /** Broadcast an event to all registered observers. */
    private void notifyObservers(TaskEvent event) {
        for (TaskObserver obs : observers) {
            obs.onTaskEvent(event);
        }
    }

    // -----------------------------------------------------------------------
    // Queue operations
    // -----------------------------------------------------------------------

    /**
     * Submit a task to the queue.
     * Task state must be PENDING; the task is rejected otherwise.
     */
    public void submit(Task task) {
        if (task.getState() != Task.State.PENDING) {
            throw new IllegalArgumentException("Only PENDING tasks can be submitted");
        }
        pendingQueue.add(task);
        notifyObservers(new TaskEvent(task, TaskEvent.Type.SUBMITTED));
    }

    /**
     * Run the next task selected by the current strategy.
     *
     * <p>This method simulates execution by advancing the task state to
     * RUNNING and then immediately to COMPLETED.  In a real system the
     * "running" phase would involve handing the task to an executor thread.
     *
     * @return the task that was executed, or {@code null} if the queue is empty
     */
    public Task runNext() {
        if (strategy == null) {
            throw new IllegalStateException("No scheduling strategy set -- call setStrategy() first");
        }
        if (pendingQueue.isEmpty()) return null;

        // Strategy selects -- it receives an unmodifiable view of the queue
        Task next = strategy.selectNext(Collections.unmodifiableList(pendingQueue));
        if (next == null) return null;

        pendingQueue.remove(next);

        // Advance lifecycle: PENDING -> RUNNING -> COMPLETED
        next.markRunning();
        notifyObservers(new TaskEvent(next, TaskEvent.Type.STARTED));

        next.markCompleted();
        notifyObservers(new TaskEvent(next, TaskEvent.Type.COMPLETED));

        return next;
    }

    /**
     * Run all pending tasks in the order selected by the current strategy.
     *
     * @return an ordered list of tasks in the sequence they were executed
     */
    public List<Task> runAll() {
        List<Task> executed = new ArrayList<>();
        Task task;
        while ((task = runNext()) != null) {
            executed.add(task);
        }
        return executed;
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /** Return the number of tasks currently waiting in the queue. */
    public int getPendingCount() { return pendingQueue.size(); }

    /** Return true if the queue is empty. */
    public boolean isEmpty() { return pendingQueue.isEmpty(); }

    // -----------------------------------------------------------------------
    // Test support
    // -----------------------------------------------------------------------

    /**
     * Clear the queue, remove all observers, and set a new strategy.
     * Intended for use between unit tests so tests don't bleed state into
     * one another.  Do not call in production code.
     */
    public void reset(SchedulingStrategy newStrategy) {
        pendingQueue.clear();
        observers.clear();
        this.strategy = newStrategy;
    }
}

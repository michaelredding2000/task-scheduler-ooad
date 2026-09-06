package scheduler.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single unit of work in the scheduling system.
 *
 * <p>A Task moves through a well-defined lifecycle:
 * PENDING → RUNNING → COMPLETED (or FAILED)
 *
 * <p>State is package-private so only the Scheduler (same package hierarchy)
 * can advance it.  External code reads state but cannot set it directly.
 */
public class Task {

    public enum State { PENDING, RUNNING, COMPLETED, FAILED }

    private final String id;
    private final String name;
    private final int priority;
    private final Instant createdAt;
    private State state;

    public Task(String name, int priority) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Task name must not be blank");
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.priority = priority;
        this.createdAt = Instant.now();
        this.state = State.PENDING;
    }

    public void markRunning() {
        if (state != State.PENDING) throw new IllegalStateException("Task must be PENDING to start; was " + state);
        state = State.RUNNING;
    }

    public void markCompleted() {
        if (state != State.RUNNING) throw new IllegalStateException("Task must be RUNNING to complete; was " + state);
        state = State.COMPLETED;
    }

    public void markFailed() {
        if (state != State.RUNNING) throw new IllegalStateException("Task must be RUNNING to fail; was " + state);
        state = State.FAILED;
    }

    public String  getId()        { return id; }
    public String  getName()      { return name; }
    public int     getPriority()  { return priority; }
    public Instant getCreatedAt() { return createdAt; }
    public State   getState()     { return state; }

    @Override
    public String toString() {
        return String.format("Task{name='%s', priority=%d, state=%s}", name, priority, state);
    }
}

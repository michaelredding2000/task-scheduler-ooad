package scheduler.observer;

import scheduler.model.Task;

public class TaskEvent {
    public enum Type { SUBMITTED, STARTED, COMPLETED, FAILED }

    private final Task task;
    private final Type type;

    public TaskEvent(Task task, Type type) {
        this.task = task;
        this.type = type;
    }

    public Task getTask() { return task; }
    public Type getType() { return type; }
}

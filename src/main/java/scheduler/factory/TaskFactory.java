package scheduler.factory;

import scheduler.model.Task;

public class TaskFactory {
    public static final int PRIORITY_HIGH   = 10;
    public static final int PRIORITY_NORMAL = 5;
    public static final int PRIORITY_LOW    = 1;

    private TaskFactory() {}

    public static Task createHighPriority(String name) { return new Task(name, PRIORITY_HIGH); }
    public static Task createNormal(String name)       { return new Task(name, PRIORITY_NORMAL); }
    public static Task createLowPriority(String name)  { return new Task(name, PRIORITY_LOW); }
    public static Task createWithPriority(String name, int priority) { return new Task(name, priority); }
}

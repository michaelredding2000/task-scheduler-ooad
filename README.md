# task-scheduler-ooad

A task-scheduling system in Java that demonstrates four classic design patterns
from the Gang of Four: **Factory**, **Observer**, **Strategy**, and **Singleton**.
Each pattern solves a specific, concrete problem in the scheduler's design.

---

## Quick Start

```bash
# Build and run all JUnit 5 tests
mvn verify

# Run the demo application
mvn package -q
java -jar target/task-scheduler-ooad-1.0.0-jar-with-dependencies.jar
```

Requirements: Java 21, Maven 3.8+

---

## Project Structure

```
src/
  main/java/scheduler/
    model/
      Task.java                    -- domain object with lifecycle state machine
    factory/
      TaskFactory.java             -- Factory: centralised task creation
    observer/
      TaskObserver.java            -- Observer: event-listener interface
      TaskEvent.java               -- event payload (task + event type)
      ConsoleLogger.java           -- concrete observer: prints to stdout
      MetricsCollector.java        -- concrete observer: counts events (test-friendly)
    strategy/
      SchedulingStrategy.java      -- Strategy: scheduling algorithm interface
      FifoStrategy.java            -- concrete strategy: first-in, first-out
      PriorityStrategy.java        -- concrete strategy: highest priority first
      RoundRobinStrategy.java      -- concrete strategy: equal rotation
    singleton/
      Scheduler.java               -- Singleton: the shared scheduler instance
    Main.java                      -- demo entry point

  test/java/scheduler/
    TaskFactoryTest.java           -- tests for Factory pattern
    ObserverTest.java              -- tests for Observer pattern
    StrategyTest.java              -- tests for Strategy pattern
    SingletonTest.java             -- tests for Singleton pattern

diagrams/
  class-diagram.md                 -- Mermaid UML class diagram
.github/workflows/
  ci.yml                           -- GitHub Actions: build + test on push
```

---

## Design Patterns

### 1. Factory -- `TaskFactory`

**Problem it solves:**  
Task objects require a name and a priority.  Without a factory, every call site
must hard-code the right priority constant.  If the constant for "high priority"
changes from 10 to 100, you'd have to find and update every occurrence.

**Design decision:**  
Static factory methods (`createHighPriority`, `createNormal`, `createLowPriority`)
encapsulate the priority constants and name validation in one place.  The call
site reads `TaskFactory.createHighPriority("handle-payment")` -- clear intent,
no magic numbers.

**Trade-off considered:**  
An Abstract Factory would let you swap the entire family of task types (useful
if you add `ScheduledTask`, `RecurringTask`, etc.).  That level of indirection
is overkill here; static methods are sufficient and simpler.

---

### 2. Observer -- `TaskObserver` / `Scheduler`

**Problem it solves:**  
The Scheduler needs to notify logging, metrics, and alerting systems every time
a task changes state.  Without Observer, the Scheduler would import and call
each subsystem directly.  Adding a fourth subscriber means modifying the
Scheduler -- violating the Open/Closed Principle.

**Design decision:**  
`TaskObserver` is a single-method interface.  The Scheduler keeps a list of
observers and calls `onTaskEvent()` on each one after every state change.
Subscribers register at startup; the Scheduler has zero knowledge of who is
listening or what they do.

`MetricsCollector` doubles as a test helper: tests register it as an observer
and assert on its counts, giving precise visibility into how many events fired.

**Trade-off considered:**  
A more sophisticated implementation might use an event bus (like Guava's
EventBus) to decouple subscription from notification further.  For a portfolio
project the explicit list makes the pattern easier to trace in a code review.

---

### 3. Strategy -- `SchedulingStrategy` / `Scheduler`

**Problem it solves:**  
Different workloads need different scheduling disciplines.  A monolithic
Scheduler with a big `switch` statement would grow every time a new algorithm
was needed and would require retesting the entire class for each change.

**Design decision:**  
`SchedulingStrategy` is an interface with one method: `selectNext(pendingTasks)`.
Three concrete strategies are provided:

| Strategy | Selects | Best for |
|---|---|---|
| `FifoStrategy` | Earliest submitted task | Batch jobs; prevents starvation |
| `PriorityStrategy` | Highest priority (ties broken by submission time) | Latency-sensitive work |
| `RoundRobinStrategy` | Round-robin by submission time | Fair resource sharing |

The strategy can be swapped at runtime (`scheduler.setStrategy(new PriorityStrategy())`)
without clearing the queue or restarting -- useful for switching behaviour under
load.

**Trade-off considered:**  
Strategy objects are stateless (except `RoundRobinStrategy`, which needs a
position counter).  Stateful strategies require care when swapped mid-queue
because the position counter doesn't reset automatically.  `RoundRobinStrategy`
exposes a `reset()` method for this.

---

### 4. Singleton -- `Scheduler`

**Problem it solves:**  
A task scheduler is a shared resource.  If two independent Scheduler instances
existed, they would maintain separate queues.  A task submitted through one
instance would be invisible to an observer registered on the other.

**Design decision:**  
`Scheduler.getInstance()` returns the single shared instance, guaranteed by
the **initialisation-on-demand holder** idiom: the instance is created lazily
when first accessed and the JVM's class-loader guarantee makes it thread-safe
without any explicit synchronisation.

```java
private static class Holder {
    static final Scheduler INSTANCE = new Scheduler();
}
public static Scheduler getInstance() { return Holder.INSTANCE; }
```

`reset(SchedulingStrategy)` exists for unit tests only.  Because the Singleton
persists across test methods, each test calls `reset()` in its `@BeforeEach` to
start with a clean queue.  Production code never calls `reset()`.

**Trade-off considered:**  
Singletons introduce global mutable state, which makes unit testing harder.
The `reset()` method mitigates this.  In a real system, dependency injection
(Spring, Guice) is often preferred over Singletons because the DI container
manages lifecycle and tests can inject fresh instances.  For this project,
Singleton is the explicit requirement, and the `reset()` workaround keeps
tests independent.

---

## UML Class Diagram

See [`diagrams/class-diagram.md`](diagrams/class-diagram.md) for the full
Mermaid diagram.  GitHub renders Mermaid natively in Markdown files.

Key relationships:

```
TaskFactory ──creates──> Task
Scheduler ──uses──> SchedulingStrategy (swappable)
Scheduler ──notifies──> TaskObserver (0..*)
Scheduler ──manages──> Task (0..*)
```

---

## CI / GitHub Actions

`.github/workflows/ci.yml` runs on every push:

1. Checks out the repo.
2. Sets up JDK 21 (Temurin) with Maven cache.
3. Runs `mvn verify` (compile + test + package).
4. Uploads Surefire test reports as a build artifact.

The pipeline fails fast on any compilation error or test failure.

---

## Interview talking points

**Why four patterns instead of two or six?**  
Each pattern addresses a distinct problem: creation (Factory), notification
(Observer), algorithm selection (Strategy), and shared state (Singleton).  They
compose naturally here: the Singleton uses the Strategy to pick tasks and the
Observer to broadcast events.

**What's the difference between Strategy and Template Method?**  
Template Method puts the invariant algorithm in a base class and subclasses fill
in the steps.  Strategy extracts the whole algorithm into a separate object,
allowing runtime swapping.  Strategy is more flexible; Template Method is simpler
when the algorithm skeleton is fixed.

**Why not use `enum` for the Singleton?**  
The `enum` Singleton is concise and serialisation-safe, but it cannot extend
a class (it can implement interfaces).  A `Scheduler` that needs to extend a base
`AbstractScheduler` would outgrow the `enum` approach.  The holder idiom is
similarly safe and more flexible.

**How would you make the Scheduler truly thread-safe?**  
Replace `ArrayList` with `ConcurrentLinkedQueue`, make `strategy` a
`volatile` reference, and wrap observer dispatch in a `ReadWriteLock`.  Or -- as
mentioned above -- hand lifecycle management to a DI container and make the
Scheduler request-scoped or application-scoped as appropriate.

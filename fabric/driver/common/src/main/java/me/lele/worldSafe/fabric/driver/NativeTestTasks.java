package me.lele.worldSafe.fabric.driver;

import java.util.IdentityHashMap;
import java.util.Map;

/** Executes a native fixture operation inside the game's ordinary entity tick. */
public final class NativeTestTasks {
    private static final Map<Object, Runnable> TASKS = new IdentityHashMap<Object, Runnable>();

    private NativeTestTasks() { }

    public static void schedule(Object entity, Runnable task) {
        if (TASKS.put(entity, task) != null) { throw new IllegalStateException("Fixture already scheduled"); }
    }

    public static void tick(Object entity) {
        Runnable task = TASKS.remove(entity);
        if (task != null) { task.run(); }
    }

    public static void clear(Object entity) { TASKS.remove(entity); }
}

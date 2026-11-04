package me.lele.worldSafe.fabric.driver;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/** Finds the actual vanilla goal installed by an entity's constructor. */
public final class NativeGoals {
    private NativeGoals() { }

    public static <T> T find(Object entity, Class<T> marker, Class<?> selector, Class<?> goal) {
        ArrayDeque<Object> pending = new ArrayDeque<Object>();
        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        pending.add(entity);
        try {
            while (!pending.isEmpty()) {
                Object value = pending.removeFirst();
                if (!seen.add(value)) { continue; }
                if (marker.isInstance(value)) { return marker.cast(value); }
                if (seen.size() > 10000) { throw new IllegalStateException("Vanilla goal graph exceeded fixture limit"); }
                if (value instanceof Iterable) {
                    for (Object item : (Iterable<?>) value) { if (item != null) { pending.add(item); } }
                    continue;
                }
                if (value instanceof Map) {
                    for (Object item : ((Map<?, ?>) value).values()) { if (item != null) { pending.add(item); } }
                    continue;
                }
                for (Class<?> type = value.getClass(); type != null; type = type.getSuperclass()) {
                    for (Field field : type.getDeclaredFields()) {
                        Class<?> fieldType = field.getType();
                        if (Modifier.isStatic(field.getModifiers()) || !(selector.isAssignableFrom(fieldType)
                                || goal.isAssignableFrom(fieldType) || Iterable.class.isAssignableFrom(fieldType)
                                || Map.class.isAssignableFrom(fieldType))) { continue; }
                        field.setAccessible(true);
                        Object item = field.get(value);
                        if (item != null) { pending.add(item); }
                    }
                }
            }
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Cannot inspect vanilla goals", error);
        }
        throw new IllegalStateException("Vanilla entity did not register goal " + marker.getSimpleName());
    }
}

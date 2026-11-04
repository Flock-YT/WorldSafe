package me.lele.worldSafe.listener;

import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListenerManagerTest {

    @Test
    void firstRegistrationFailureCleansAttemptAndKeepsPreviousState() {
        Registry registry = new Registry();
        ListenerManager manager = manager(registry);
        TestListener old = new TestListener("old");
        AtomicReference<String> config = new AtomicReference<String>();
        assertTrue(manager.replace(Collections.<Listener>singletonList(old), () -> config.set("old")));

        TestListener replacement = new TestListener("replacement");
        registry.failWithRuntime(replacement);
        assertFalse(manager.replace(Collections.<Listener>singletonList(replacement), () -> config.set("new")));

        assertEquals(Collections.singleton(old), registry.registered);
        assertEquals(Collections.<Listener>singletonList(old), manager.getListeners());
        assertEquals("old", config.get());
        assertTrue(registry.unregistered.contains(replacement));
        assertFalse(registry.unregistered.contains(old));
    }

    @Test
    void middleRegistrationFailureCleansEveryAttemptedListener() {
        Registry registry = new Registry();
        ListenerManager manager = manager(registry);
        TestListener old = new TestListener("old");
        manager.replace(Collections.<Listener>singletonList(old), () -> { });

        TestListener first = new TestListener("first");
        TestListener second = new TestListener("second");
        TestListener neverAttempted = new TestListener("never-attempted");
        registry.failWithRuntime(second);
        assertFalse(manager.replace(Arrays.<Listener>asList(first, second, neverAttempted), () -> { }));

        assertEquals(Collections.singleton(old), registry.registered);
        assertTrue(registry.unregistered.contains(first));
        assertTrue(registry.unregistered.contains(second));
        assertFalse(registry.unregistered.contains(neverAttempted));
        assertEquals(Collections.<Listener>singletonList(old), manager.getListeners());
    }

    @Test
    void linkageErrorUsesTheSameAtomicCleanupPath() {
        Registry registry = new Registry();
        ListenerManager manager = manager(registry);
        TestListener old = new TestListener("old");
        manager.replace(Collections.<Listener>singletonList(old), () -> { });

        TestListener replacement = new TestListener("replacement");
        registry.failWithLinkageError(replacement);
        assertFalse(manager.replace(Collections.<Listener>singletonList(replacement), () -> { }));

        assertEquals(Collections.singleton(old), registry.registered);
        assertEquals(Collections.<Listener>singletonList(old), manager.getListeners());
        assertTrue(registry.messages.get(0).contains("NoClassDefFoundError"));
    }

    @Test
    void successfulReplacementRegistersAllNewListenersBeforeRemovingOldOnes() {
        Registry registry = new Registry();
        ListenerManager manager = manager(registry);
        TestListener old = new TestListener("old");
        manager.replace(Collections.<Listener>singletonList(old), () -> { });
        registry.operations.clear();
        registry.unregistered.clear();
        registry.requiredDuringRegistration = old;

        TestListener first = new TestListener("first");
        TestListener second = new TestListener("second");
        assertTrue(manager.replace(Arrays.<Listener>asList(first, second),
                () -> registry.operations.add("commit")));

        assertTrue(registry.oldListenerPresentDuringEveryRegistration);
        assertEquals(Arrays.asList("register:first", "register:second", "unregister:old", "commit"),
                registry.operations);
        assertEquals(new LinkedHashSet<Listener>(Arrays.<Listener>asList(first, second)), registry.registered);
        assertEquals(Arrays.<Listener>asList(first, second), manager.getListeners());
    }

    private ListenerManager manager(Registry registry) {
        return new ListenerManager(registry::register, registry::unregister, registry.messages::add);
    }

    private static final class Registry {
        private final Set<Listener> registered = new LinkedHashSet<Listener>();
        private final List<Listener> unregistered = new ArrayList<Listener>();
        private final List<String> messages = new ArrayList<String>();
        private final List<String> operations = new ArrayList<String>();
        private Listener runtimeFailure;
        private Listener linkageFailure;
        private Listener requiredDuringRegistration;
        private boolean oldListenerPresentDuringEveryRegistration = true;

        private void register(Listener listener) {
            if (requiredDuringRegistration != null && !registered.contains(requiredDuringRegistration)) {
                oldListenerPresentDuringEveryRegistration = false;
            }
            operations.add("register:" + listener);
            registered.add(listener);
            if (listener == runtimeFailure) {
                throw new IllegalStateException("registration failed");
            }
            if (listener == linkageFailure) {
                throw new NoClassDefFoundError("missing-listener-class");
            }
        }

        private void unregister(Listener listener) {
            operations.add("unregister:" + listener);
            unregistered.add(listener);
            registered.remove(listener);
        }

        private void failWithRuntime(Listener listener) {
            runtimeFailure = listener;
        }

        private void failWithLinkageError(Listener listener) {
            linkageFailure = listener;
        }
    }

    private static final class TestListener implements Listener {
        private final String name;

        private TestListener(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

package me.lele.worldSafe;

import me.lele.worldSafe.compat.ServerCapabilities;
import me.lele.worldSafe.feature.FeatureDefinition;
import me.lele.worldSafe.listener.blocks.explosionprevention.BedExplosionProtectionListener;
import me.lele.worldSafe.listener.blocks.explosionprevention.RespawnAnchorExplosionPreventionListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListenerPriorityTest {

    @Test
    void modifyingHandlersUseHighestAndSourceObserversUseMonitor() {
        ServerCapabilities capabilities = ServerCapabilities.forTesting(
                Collections.<ServerCapabilities.Capability>emptySet(),
                Collections.<String>emptySet(), Collections.<String>emptySet());
        int observerCount = 0;
        int modifyingCount = 0;
        Set<Class<?>> inspectedClasses = new LinkedHashSet<Class<?>>();

        for (FeatureDefinition feature : WorldSafe.FEATURES) {
            Listener listener = feature.createListener(Collections.singletonList("world"), capabilities);
            if (!inspectedClasses.add(listener.getClass())) {
                continue;
            }
            for (Method method : listener.getClass().getDeclaredMethods()) {
                EventHandler annotation = method.getAnnotation(EventHandler.class);
                if (annotation == null) {
                    continue;
                }
                String handler = listener.getClass().getSimpleName() + "." + method.getName();
                if (isSourceObserver(listener, method)) {
                    assertEquals(EventPriority.MONITOR, annotation.priority(), handler);
                    assertEquals(true, annotation.ignoreCancelled(), handler);
                    observerCount++;
                } else {
                    assertEquals(EventPriority.HIGHEST, annotation.priority(), handler);
                    assertEquals(false, annotation.ignoreCancelled(), handler);
                    modifyingCount++;
                }
            }
        }

        assertEquals(2, observerCount);
        assertEquals(49, modifyingCount);
    }

    private boolean isSourceObserver(Listener listener, Method method) {
        return (listener instanceof BedExplosionProtectionListener && "onBedUse".equals(method.getName()))
                || (listener instanceof RespawnAnchorExplosionPreventionListener
                && "onAnchorUse".equals(method.getName()));
    }
}

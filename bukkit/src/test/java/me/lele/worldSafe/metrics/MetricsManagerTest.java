package me.lele.worldSafe.metrics;

import org.bstats.bukkit.Metrics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MetricsManagerTest {

    @Test
    void createsAndShutsDownExactlyOncePerStateTransition() {
        Metrics first = mock(Metrics.class);
        Metrics second = mock(Metrics.class);
        AtomicInteger creations = new AtomicInteger();
        MetricsManager manager = new MetricsManager(
                () -> creations.getAndIncrement() == 0 ? first : second,
                message -> { });

        manager.sync(true);
        manager.sync(true);
        assertTrue(manager.isRunning());
        manager.sync(false);
        manager.sync(false);
        assertFalse(manager.isRunning());
        manager.sync(true);
        manager.shutdown();

        verify(first, times(1)).shutdown();
        verify(second, times(1)).shutdown();
        assertTrue(creations.get() == 2);
    }

    @Test
    void startupFailureWarnsWithoutPreventingLaterRetry() {
        Metrics metrics = mock(Metrics.class);
        AtomicInteger attempts = new AtomicInteger();
        List<String> warnings = new ArrayList<String>();
        MetricsManager manager = new MetricsManager(() -> {
            if (attempts.getAndIncrement() == 0) {
                throw new IllegalStateException("not ready");
            }
            return metrics;
        }, warnings::add);

        manager.sync(true);
        assertFalse(manager.isRunning());
        assertTrue(warnings.get(0).contains("not ready"));

        manager.sync(true);
        assertTrue(manager.isRunning());
        assertTrue(attempts.get() == 2);
    }

    @Test
    void linkageFailureIsContained() {
        List<String> warnings = new ArrayList<String>();
        MetricsManager manager = new MetricsManager(() -> {
            throw new NoClassDefFoundError("missing-bstats-class");
        }, warnings::add);

        manager.sync(true);

        assertFalse(manager.isRunning());
        assertTrue(warnings.get(0).contains("NoClassDefFoundError"));
    }

    @Test
    void shutdownFailureStillLeavesManagerStopped() {
        Metrics metrics = mock(Metrics.class);
        doThrow(new IllegalStateException("shutdown failed")).when(metrics).shutdown();
        List<String> warnings = new ArrayList<String>();
        MetricsManager manager = new MetricsManager(() -> metrics, warnings::add);
        manager.sync(true);

        manager.shutdown();

        assertFalse(manager.isRunning());
        assertTrue(warnings.get(0).contains("shutdown failed"));
    }
}

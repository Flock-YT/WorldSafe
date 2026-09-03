package me.lele.worldSafe.metrics;

import org.bstats.bukkit.Metrics;

import java.util.Objects;
import java.util.function.Consumer;

public final class MetricsManager {

    public interface Factory {
        Metrics create();
    }

    private final Factory factory;
    private final Consumer<String> warningLogger;
    private Metrics metrics;

    public MetricsManager(Factory factory, Consumer<String> warningLogger) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.warningLogger = Objects.requireNonNull(warningLogger, "warningLogger");
    }

    public void sync(boolean enabled) {
        if (!enabled) {
            shutdown();
            return;
        }
        if (metrics != null) {
            return;
        }

        try {
            Metrics created = factory.create();
            if (created == null) {
                warningLogger.accept("bStats could not be started: metrics factory returned null.");
                return;
            }
            metrics = created;
        } catch (RuntimeException exception) {
            warningLogger.accept("bStats could not be started: " + describe(exception));
        } catch (LinkageError error) {
            warningLogger.accept("bStats is unavailable on this server: " + describe(error));
        }
    }

    public void shutdown() {
        if (metrics == null) {
            return;
        }

        Metrics active = metrics;
        metrics = null;
        try {
            active.shutdown();
        } catch (RuntimeException exception) {
            warningLogger.accept("bStats could not be shut down cleanly: " + describe(exception));
        } catch (LinkageError error) {
            warningLogger.accept("bStats could not be shut down cleanly: " + describe(error));
        }
    }

    boolean isRunning() {
        return metrics != null;
    }

    private String describe(Throwable failure) {
        String message = failure.getMessage();
        return message == null || message.trim().isEmpty()
                ? failure.getClass().getSimpleName()
                : failure.getClass().getSimpleName() + ": " + message;
    }
}

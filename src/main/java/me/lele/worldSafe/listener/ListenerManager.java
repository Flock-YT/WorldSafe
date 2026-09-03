package me.lele.worldSafe.listener;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ListenerManager {

    private final Consumer<Listener> registrar;
    private final Consumer<Listener> unregistrar;
    private final Consumer<String> failureLogger;
    private final List<Listener> listeners = new ArrayList<Listener>();

    public ListenerManager(Consumer<Listener> registrar, Consumer<Listener> unregistrar,
            Consumer<String> failureLogger) {
        this.registrar = Objects.requireNonNull(registrar, "registrar");
        this.unregistrar = Objects.requireNonNull(unregistrar, "unregistrar");
        this.failureLogger = Objects.requireNonNull(failureLogger, "failureLogger");
    }

    public boolean replace(List<Listener> replacements, Runnable commit) {
        Objects.requireNonNull(replacements, "replacements");
        Objects.requireNonNull(commit, "commit");

        List<Listener> candidates = new ArrayList<Listener>(replacements);
        List<Listener> attempted = new ArrayList<Listener>();
        try {
            for (Listener listener : candidates) {
                attempted.add(listener);
                registrar.accept(listener);
            }
        } catch (RuntimeException exception) {
            handleRegistrationFailure(attempted, exception);
            return false;
        } catch (LinkageError error) {
            handleRegistrationFailure(attempted, error);
            return false;
        }

        List<Listener> previous = new ArrayList<Listener>(listeners);
        for (Listener listener : previous) {
            unregistrar.accept(listener);
        }
        listeners.clear();
        listeners.addAll(candidates);
        commit.run();
        return true;
    }

    public void clear() {
        List<Listener> previous = new ArrayList<Listener>(listeners);
        listeners.clear();
        for (Listener listener : previous) {
            unregisterAfterFailure(listener);
        }
    }

    public List<Listener> getListeners() {
        return Collections.unmodifiableList(new ArrayList<Listener>(listeners));
    }

    private void handleRegistrationFailure(List<Listener> attempted, Throwable failure) {
        for (Listener listener : attempted) {
            unregisterAfterFailure(listener);
        }
        failureLogger.accept("Listener replacement failed; previous listeners remain active: "
                + describe(failure));
    }

    private void unregisterAfterFailure(Listener listener) {
        try {
            unregistrar.accept(listener);
        } catch (RuntimeException exception) {
            failureLogger.accept("Failed to clean up listener '" + listener.getClass().getName() + "': "
                    + describe(exception));
        } catch (LinkageError error) {
            failureLogger.accept("Failed to clean up listener '" + listener.getClass().getName() + "': "
                    + describe(error));
        }
    }

    private String describe(Throwable failure) {
        String message = failure.getMessage();
        return message == null || message.trim().isEmpty()
                ? failure.getClass().getSimpleName()
                : failure.getClass().getSimpleName() + ": " + message;
    }
}

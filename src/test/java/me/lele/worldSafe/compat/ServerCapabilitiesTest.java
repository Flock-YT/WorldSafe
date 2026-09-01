package me.lele.worldSafe.compat;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerCapabilitiesTest {

    @Test
    void detectsMissingModernMethodsOnCompileBaseline() {
        ServerCapabilities capabilities = ServerCapabilities.detect();
        assertFalse(capabilities.has(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE));
        assertFalse(capabilities.has(ServerCapabilities.Capability.RESPAWN_ANCHOR_CHARGES));
        assertFalse(capabilities.has(ServerCapabilities.Capability.PROJECTILE_HIT_BLOCK));
    }

    @Test
    void capabilitySnapshotMatchesAliases() {
        ServerCapabilities capabilities = ServerCapabilities.forTesting(
                EnumSet.of(ServerCapabilities.Capability.PROJECTILE_HIT_BLOCK),
                Collections.singleton("LEGACY_SOIL"), Collections.singleton("ENDER_CRYSTAL"));
        assertTrue(capabilities.hasMaterial("FARMLAND"));
        assertTrue(capabilities.hasEntityType("END_CRYSTAL"));
        assertTrue(capabilities.has(ServerCapabilities.Capability.PROJECTILE_HIT_BLOCK));
    }

    @Test
    void onlyCancelsEventsThatImplementCancellable() {
        ServerCapabilities capabilities = ServerCapabilities.forTesting(
                Collections.<ServerCapabilities.Capability>emptySet(), Collections.<String>emptySet(),
                Collections.<String>emptySet());
        TestCancellableEvent cancellable = new TestCancellableEvent();
        assertTrue(capabilities.cancelIfPossible(cancellable));
        assertTrue(cancellable.isCancelled());
        assertFalse(capabilities.cancelIfPossible(new TestEvent()));
    }

    private static class TestEvent extends Event {
        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }

    private static final class TestCancellableEvent extends TestEvent implements Cancellable {
        private boolean cancelled;

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            cancelled = cancel;
        }
    }
}

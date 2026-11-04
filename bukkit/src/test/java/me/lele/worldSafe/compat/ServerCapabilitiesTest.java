package me.lele.worldSafe.compat;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockExplodeEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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

    @Test
    void invocationTargetFailureWarnsOnceAndLaterCallsStillSucceed() throws Exception {
        BlockState state = mock(BlockState.class);
        FlakyBlockExplodeEvent event = new FlakyBlockExplodeEvent(mock(Block.class), state);
        Method method = FlakyBlockExplodeEvent.class.getMethod("getExplodedBlockState");
        List<Throwable> failures = new ArrayList<Throwable>();
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE), method,
                null, null, null, null, (capability, failure) -> failures.add(failure));

        assertNull(capabilities.getExplodedBlockState(event));
        assertSame(state, capabilities.getExplodedBlockState(event));
        assertSame(state, capabilities.getExplodedBlockState(event));
        assertEquals(1, failures.size());
        assertEquals("InvocationTargetException", failures.get(0).getClass().getSimpleName());
        assertTrue(capabilities.has(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE));
    }

    @Test
    void illegalAccessFailureWarnsOnlyOncePerCapability() throws Exception {
        Method method = PrivateStateEvent.class.getDeclaredMethod("hiddenState");
        List<Throwable> failures = new ArrayList<Throwable>();
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE), method,
                null, null, null, null, (capability, failure) -> failures.add(failure));
        PrivateStateEvent event = new PrivateStateEvent(mock(Block.class));

        assertNull(capabilities.getExplodedBlockState(event));
        assertNull(capabilities.getExplodedBlockState(event));
        assertEquals(1, failures.size());
        assertEquals("IllegalAccessException", failures.get(0).getClass().getSimpleName());
    }

    @Test
    void illegalArgumentFailureWarnsOnlyOncePerCapability() throws Exception {
        Method method = WrongTarget.class.getMethod("getState");
        List<Throwable> failures = new ArrayList<Throwable>();
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE), method,
                null, null, null, null, (capability, failure) -> failures.add(failure));
        BlockExplodeEvent event = new BlockExplodeEvent(mock(Block.class), new ArrayList<Block>(), 1.0f);

        assertNull(capabilities.getExplodedBlockState(event));
        assertNull(capabilities.getExplodedBlockState(event));
        assertEquals(1, failures.size());
        assertEquals("IllegalArgumentException", failures.get(0).getClass().getSimpleName());
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

    public static final class FlakyBlockExplodeEvent extends BlockExplodeEvent {
        private final BlockState state;
        private int calls;

        private FlakyBlockExplodeEvent(Block block, BlockState state) {
            super(block, new ArrayList<Block>(), 1.0f);
            this.state = state;
        }

        public BlockState getExplodedBlockState() {
            if (calls++ == 0) {
                throw new IllegalStateException("temporary failure");
            }
            return state;
        }
    }

    private static final class PrivateStateEvent extends BlockExplodeEvent {
        private PrivateStateEvent(Block block) {
            super(block, new ArrayList<Block>(), 1.0f);
        }

        @SuppressWarnings("unused")
        private BlockState hiddenState() {
            return null;
        }
    }

    public static final class WrongTarget {
        public BlockState getState() {
            return null;
        }
    }
}

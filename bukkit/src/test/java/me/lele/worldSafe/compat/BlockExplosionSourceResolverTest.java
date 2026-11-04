package me.lele.worldSafe.compat;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockExplodeEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlockExplosionSourceResolverTest {

    @Test
    void throwingSnapshotMethodFallsBackAndConsumesCache() throws Exception {
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE),
                FailingSnapshotEvent.class.getMethod("getExplodedBlockState"), null, null, null, null);
        Block block = block(Material.BED_BLOCK);
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(capabilities);
        resolver.remember(block);
        when(block.getType()).thenReturn(Material.AIR);
        assertTrue(resolver.isSource(new FailingSnapshotEvent(block), "BED_BLOCK"));
        assertFalse(resolver.isSource(new FailingSnapshotEvent(block), "BED_BLOCK"));
    }

    @Test
    void ttlBoundaryRemainsInclusive() {
        AtomicLong clock = new AtomicLong();
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(clock::get, 10L);
        Block block = block(Material.BED_BLOCK);
        resolver.remember(block);
        when(block.getType()).thenReturn(Material.AIR);
        clock.set(10L);
        assertTrue(resolver.isSource(explosion(block), "BED_BLOCK"));
    }

    @Test
    void emptySnapshotsAndInvocationFailuresAllowOneFallback() throws Exception {
        Method method = ModernBlockExplodeEvent.class.getMethod("getExplodedBlockState");
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE), method, null, null, null, null);
        for (String name : new String[] {"AIR", "CAVE_AIR", "VOID_AIR", "null-state", "null-type", "failure"}) {
            Block block = block(Material.BED_BLOCK);
            BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(capabilities);
            resolver.remember(block);
            when(block.getType()).thenReturn(Material.AIR);
            BlockState state = mock(BlockState.class);
            if (!name.equals("null-type")) {
                Material material = mock(Material.class);
                when(material.name()).thenReturn(name);
                when(state.getType()).thenReturn(material);
            }
            BlockExplodeEvent event = name.equals("failure") ? explosion(block)
                    : new ModernBlockExplodeEvent(block, name.equals("null-state") ? null : state);
            assertTrue(resolver.isSource(event, "BED_BLOCK"), name);
            assertFalse(resolver.isSource(explosion(block), "BED_BLOCK"), name);
        }
    }

    @Test
    void pairedRecordsExpireAndConsumeTogetherOnAllDecisionPaths() throws Exception {
        Method method = ModernBlockExplodeEvent.class.getMethod("getExplodedBlockState");
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE), method, null, null, null, null);
        for (String path : new String[] {"expired", "fallback", "current", "snapshot-match", "snapshot-conflict"}) {
            AtomicLong clock = new AtomicLong();
            BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(capabilities, clock::get, 10L);
            Block foot = bedHalf(false);
            Block head = bedHalf(true);
            when(foot.getRelative(BlockFace.EAST)).thenReturn(head);
            when(head.getRelative(BlockFace.WEST)).thenReturn(foot);
            resolver.remember(foot);
            when(foot.getType()).thenReturn(Material.AIR);
            if (!path.equals("current")) {
                when(head.getType()).thenReturn(Material.AIR);
            }
            if (path.equals("expired")) {
                clock.set(11L);
            }
            BlockExplodeEvent event = explosion(head);
            if (path.startsWith("snapshot")) {
                BlockState state = mock(BlockState.class);
                when(state.getType()).thenReturn(path.equals("snapshot-match") ? Material.BED_BLOCK : Material.TNT);
                event = new ModernBlockExplodeEvent(head, state);
            }
            boolean expected = !path.equals("expired") && !path.equals("snapshot-conflict");
            org.junit.jupiter.api.Assertions.assertEquals(expected, resolver.isSource(event, "BED_BLOCK"), path);
            when(head.getType()).thenReturn(Material.AIR);
            assertFalse(resolver.isSource(explosion(foot), "BED_BLOCK"), path);
            assertFalse(resolver.isSource(explosion(head), "BED_BLOCK"), path);
        }
    }

    @Test
    void replacingOneHalfRemovesOldGroupWithoutConsumingNewRecord() {
        Block foot = bedHalf(false);
        Block head = bedHalf(true);
        when(foot.getRelative(BlockFace.EAST)).thenReturn(head);
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver();
        resolver.remember(foot);
        when(head.getType()).thenReturn(Material.TNT);
        resolver.remember(head);
        when(head.getType()).thenReturn(Material.AIR);
        when(foot.getType()).thenReturn(Material.AIR);
        assertFalse(resolver.isSource(explosion(foot), "BED_BLOCK"));
        assertTrue(resolver.isSource(explosion(head), "TNT"));
    }

    @Test
    void sameCoordinatesInOtherWorldDoNotConsumeRecord() {
        Block original = block(Material.BED_BLOCK);
        Block elsewhere = block(Material.AIR);
        World other = mock(World.class);
        when(other.getName()).thenReturn("another_world");
        when(elsewhere.getWorld()).thenReturn(other);
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver();
        resolver.remember(original);
        when(original.getType()).thenReturn(Material.AIR);
        assertFalse(resolver.isSource(explosion(elsewhere), "BED_BLOCK"));
        assertTrue(resolver.isSource(explosion(original), "BED_BLOCK"));
    }

    private Block bedHalf(boolean head) {
        Block block = block(Material.BED_BLOCK);
        when(block.getX()).thenReturn(head ? 2 : 1);
        org.bukkit.material.Bed data = new org.bukkit.material.Bed(BlockFace.EAST);
        data.setHeadOfBed(head);
        BlockState state = mock(BlockState.class);
        when(state.getData()).thenReturn(data);
        when(block.getState()).thenReturn(state);
        return block;
    }

    private BlockExplodeEvent explosion(Block block) {
        return new BlockExplodeEvent(block, new ArrayList<Block>(), 1.0f);
    }

    @Test
    void meaningfulSnapshotOverridesConflictingCacheAndCurrentBlock() throws Exception {
        Method method = ModernBlockExplodeEvent.class.getMethod("getExplodedBlockState");
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE), method, null, null, null, null);
        for (boolean bedCached : new boolean[] {true, false}) {
            Material anchor = mock(Material.class);
            when(anchor.name()).thenReturn("RESPAWN_ANCHOR");
            Material cached = bedCached ? Material.BED_BLOCK : anchor;
            Material actual = bedCached ? anchor : Material.BED_BLOCK;
            Block block = block(cached);
            BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(capabilities);
            resolver.remember(block);
            BlockState state = mock(BlockState.class);
            when(state.getType()).thenReturn(actual);
            assertFalse(resolver.isSource(new ModernBlockExplodeEvent(block, state), cached.name()));
            when(block.getType()).thenReturn(Material.AIR);
            assertFalse(resolver.isSource(new BlockExplodeEvent(block, new ArrayList<Block>(), 1.0f), cached.name()));
        }
    }

    @Test
    void resolvesOldApiExplosionFromRecentInteractionLocation() {
        AtomicLong clock = new AtomicLong();
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(ServerCapabilities.detect(),
                clock::get, 10L);
        Block block = block(Material.BED_BLOCK);

        resolver.remember(block);
        when(block.getType()).thenReturn(Material.AIR);

        BlockExplodeEvent event = new BlockExplodeEvent(block, new ArrayList<Block>(), 1.0f);
        assertTrue(resolver.isSource(event, "RED_BED"));
        assertFalse(resolver.isSource(event, "RED_BED"));
    }

    @Test
    void expiresOldInteractionRecords() {
        AtomicLong clock = new AtomicLong();
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(ServerCapabilities.detect(),
                clock::get, 10L);
        Block block = block(Material.BED_BLOCK);
        resolver.remember(block);
        when(block.getType()).thenReturn(Material.AIR);
        clock.set(11L);
        assertFalse(resolver.isSource(new BlockExplodeEvent(block, new ArrayList<Block>(), 1.0f), "BED_BLOCK"));
    }

    @Test
    void usesCachedExplodedBlockStateMethodWhenAvailable() throws Exception {
        Method method = ModernBlockExplodeEvent.class.getMethod("getExplodedBlockState");
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE), method, null, null, null, null);
        BlockState state = mock(BlockState.class);
        when(state.getType()).thenReturn(Material.TNT);
        ModernBlockExplodeEvent event = new ModernBlockExplodeEvent(block(Material.AIR), state);
        assertTrue(new BlockExplosionSourceResolver(capabilities).isSource(event, "TNT"));
    }

    @Test
    void directBlockMatchConsumesCachedInteractionRecord() {
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(ServerCapabilities.detect());
        Block block = block(Material.BED_BLOCK);
        resolver.remember(block);

        assertTrue(resolver.isSource(new BlockExplodeEvent(block, new ArrayList<Block>(), 1.0f), "BED_BLOCK"));
        when(block.getType()).thenReturn(Material.AIR);
        assertFalse(resolver.isSource(new BlockExplodeEvent(block, new ArrayList<Block>(), 1.0f), "BED_BLOCK"));
    }

    @Test
    void explodedStateMatchConsumesCachedInteractionRecord() throws Exception {
        Method method = ModernBlockExplodeEvent.class.getMethod("getExplodedBlockState");
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.EXPLODED_BLOCK_STATE), method, null, null, null, null);
        Block block = block(Material.BED_BLOCK);
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(capabilities);
        resolver.remember(block);
        when(block.getType()).thenReturn(Material.AIR);
        BlockState state = mock(BlockState.class);
        when(state.getType()).thenReturn(Material.BED_BLOCK);

        assertTrue(resolver.isSource(new ModernBlockExplodeEvent(block, state), "BED_BLOCK"));
        assertFalse(resolver.isSource(new BlockExplodeEvent(block, new ArrayList<Block>(), 1.0f), "BED_BLOCK"));
    }

    private Block block(Material material) {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        Block block = mock(Block.class);
        when(block.getWorld()).thenReturn(world);
        when(block.getType()).thenReturn(material);
        when(block.getX()).thenReturn(1);
        when(block.getY()).thenReturn(64);
        when(block.getZ()).thenReturn(2);
        return block;
    }

    public static final class ModernBlockExplodeEvent extends BlockExplodeEvent {
        private final BlockState state;

        private ModernBlockExplodeEvent(Block block, BlockState state) {
            super(block, new ArrayList<Block>(), 1.0f);
            this.state = state;
        }

        public BlockState getExplodedBlockState() {
            return state;
        }
    }

    public static final class FailingSnapshotEvent extends BlockExplodeEvent {
        FailingSnapshotEvent(Block block) {
            super(block, new ArrayList<Block>(), 1.0f);
        }

        public BlockState getExplodedBlockState() {
            throw new IllegalStateException("snapshot unavailable");
        }
    }
}

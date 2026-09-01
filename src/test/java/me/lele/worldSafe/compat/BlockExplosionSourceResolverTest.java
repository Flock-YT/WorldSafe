package me.lele.worldSafe.compat;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockExplodeEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlockExplosionSourceResolverTest {

    @Test
    void resolvesOldApiExplosionFromRecentInteractionLocation() {
        AtomicLong clock = new AtomicLong();
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(clock::get,
                Duration.ofSeconds(2).toNanos());
        World world = world("world_nether");
        Block block = block(world, 4, 70, -3, Material.RED_BED);

        resolver.remember(block);
        when(block.getType()).thenReturn(Material.AIR);

        BlockExplodeEvent event = new BlockExplodeEvent(block, new ArrayList<>(), 1.0f);
        assertTrue(resolver.isSource(event, "RED_BED"));
        assertFalse(resolver.isSource(event, "RED_BED"), "a fallback record is single-use");
    }

    @Test
    void expiresOldInteractionRecords() {
        AtomicLong clock = new AtomicLong();
        BlockExplosionSourceResolver resolver = new BlockExplosionSourceResolver(clock::get, 10L);
        Block block = block(world("world"), 1, 2, 3, Material.RESPAWN_ANCHOR);

        resolver.remember(block);
        when(block.getType()).thenReturn(Material.AIR);
        clock.set(11L);

        assertFalse(resolver.isSource(new BlockExplodeEvent(block, new ArrayList<>(), 1.0f),
                "RESPAWN_ANCHOR"));
    }

    @Test
    void usesExplodedBlockStateWhenRuntimeApiProvidesIt() {
        Block block = block(world("world"), 1, 2, 3, Material.AIR);
        BlockState state = mock(BlockState.class);
        when(state.getType()).thenReturn(Material.RESPAWN_ANCHOR);
        BlockExplodeEvent event = new ModernBlockExplodeEvent(block, state);

        assertTrue(new BlockExplosionSourceResolver().isSource(event, "RESPAWN_ANCHOR"));
    }

    private World world(String name) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(name);
        return world;
    }

    private Block block(World world, int x, int y, int z, Material material) {
        Block block = mock(Block.class);
        when(block.getWorld()).thenReturn(world);
        when(block.getX()).thenReturn(x);
        when(block.getY()).thenReturn(y);
        when(block.getZ()).thenReturn(z);
        when(block.getType()).thenReturn(material);
        return block;
    }

    public static final class ModernBlockExplodeEvent extends BlockExplodeEvent {
        private final BlockState explodedBlockState;

        private ModernBlockExplodeEvent(Block block, BlockState explodedBlockState) {
            super(block, new ArrayList<>(), 1.0f);
            this.explodedBlockState = explodedBlockState;
        }

        public BlockState getExplodedBlockState() {
            return explodedBlockState;
        }
    }
}

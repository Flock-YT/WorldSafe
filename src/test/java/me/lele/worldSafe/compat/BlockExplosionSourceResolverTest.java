package me.lele.worldSafe.compat;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
}

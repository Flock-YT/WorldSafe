package me.lele.worldSafe.compat;

import me.lele.worldSafe.listener.blocks.explosioncancel.BedExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosioncancel.RespawnAnchorExplosionCancelListener;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CancellationInteractionCacheTest {

    @Test
    void cancelledBedUseDoesNotLeaveExplosionSourceRecord() {
        World world = world("world_nether", World.Environment.NETHER);
        Block bed = block(world, Material.BED_BLOCK);
        BedExplosionCancelListener listener = new BedExplosionCancelListener(
                Collections.singletonList("world_nether"));
        PlayerInteractEvent interaction = interaction(world, bed);

        listener.onPlayerInteractEvent(interaction);
        assertTrue(interaction.isCancelled());

        when(bed.getType()).thenReturn(Material.AIR);
        BlockExplodeEvent explosion = new BlockExplodeEvent(bed, new ArrayList<Block>(), 1.0f);
        listener.onBedExplosion(explosion);
        assertFalse(explosion.isCancelled());
    }

    @Test
    void cancelledRespawnAnchorUseDoesNotLeaveExplosionSourceRecord() throws Exception {
        Method getBlockData = ModernBlock.class.getMethod("getBlockData");
        Method getCharges = AnchorData.class.getMethod("getCharges");
        Method getMaximumCharges = AnchorData.class.getMethod("getMaximumCharges");
        ServerCapabilities capabilities = ServerCapabilities.forTestingWithMethods(
                EnumSet.of(ServerCapabilities.Capability.RESPAWN_ANCHOR_CHARGES), null,
                getBlockData, getCharges, getMaximumCharges, null);
        World world = world("world", World.Environment.NORMAL);
        ModernBlock anchor = mock(ModernBlock.class);
        Material anchorMaterial = mock(Material.class);
        when(anchorMaterial.name()).thenReturn("RESPAWN_ANCHOR");
        when(anchor.getType()).thenReturn(anchorMaterial);
        when(anchor.getWorld()).thenReturn(world);
        when(anchor.getX()).thenReturn(1);
        when(anchor.getY()).thenReturn(64);
        when(anchor.getZ()).thenReturn(2);
        when(anchor.getBlockData()).thenReturn(new AnchorData(1, 4));
        RespawnAnchorExplosionCancelListener listener = new RespawnAnchorExplosionCancelListener(
                Collections.singletonList("world"), capabilities);
        PlayerInteractEvent interaction = interaction(world, anchor);

        listener.onAnchorUse(interaction);
        assertTrue(interaction.isCancelled());

        when(anchor.getType()).thenReturn(Material.AIR);
        BlockExplodeEvent explosion = new BlockExplodeEvent(anchor, new ArrayList<Block>(), 1.0f);
        listener.onRespawnAnchorExplosion(explosion);
        assertFalse(explosion.isCancelled());
    }

    private PlayerInteractEvent interaction(World world, Block block) {
        Player player = mock(Player.class);
        when(player.getWorld()).thenReturn(world);
        return new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, block,
                org.bukkit.block.BlockFace.UP);
    }

    private Block block(World world, Material material) {
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(material);
        when(block.getWorld()).thenReturn(world);
        when(block.getX()).thenReturn(1);
        when(block.getY()).thenReturn(64);
        when(block.getZ()).thenReturn(2);
        return block;
    }

    private World world(String name, World.Environment environment) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(name);
        when(world.getEnvironment()).thenReturn(environment);
        return world;
    }

    private interface ModernBlock extends Block {
        AnchorData getBlockData();
    }

    public static final class AnchorData {
        private final int charges;
        private final int maximumCharges;

        private AnchorData(int charges, int maximumCharges) {
            this.charges = charges;
            this.maximumCharges = maximumCharges;
        }

        public int getCharges() {
            return charges;
        }

        public int getMaximumCharges() {
            return maximumCharges;
        }
    }
}

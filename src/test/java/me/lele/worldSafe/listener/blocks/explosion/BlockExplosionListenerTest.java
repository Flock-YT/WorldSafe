package me.lele.worldSafe.listener.blocks.explosion;

import me.lele.worldSafe.listener.blocks.explosioncancel.BedExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosioncancel.RespawnAnchorExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosionprevention.BedExplosionProtectionListener;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlockExplosionListenerTest {

    @Test
    void bedCancelStopsInvalidDimensionInteraction() {
        World world = world("world_nether", World.Environment.NETHER);
        Block bed = block(world, Material.RED_BED, mock(Bed.class));
        PlayerInteractEvent event = interaction(bed);

        new BedExplosionCancelListener(List.of("world_nether")).onPlayerInteractEvent(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void bedProtectionUsesOldApiFallbackAndKeepsExplosionActive() {
        World world = world("world_nether", World.Environment.NETHER);
        Block bed = block(world, Material.RED_BED, mock(Bed.class));
        BedExplosionProtectionListener listener = new BedExplosionProtectionListener(List.of("world_nether"));
        listener.onBedUse(interaction(bed));

        when(bed.getType()).thenReturn(Material.AIR);
        List<Block> affected = new ArrayList<>(List.of(mock(Block.class)));
        BlockExplodeEvent explosion = new BlockExplodeEvent(bed, affected, 1.0f);
        listener.onBedExplosion(explosion);

        assertFalse(explosion.isCancelled());
        assertTrue(explosion.blockList().isEmpty());
    }

    @Test
    void chargedAnchorCancelStopsOverworldExplosionButNotNetherUse() {
        RespawnAnchor anchorData = mock(RespawnAnchor.class);
        when(anchorData.getCharges()).thenReturn(1);
        when(anchorData.getMaximumCharges()).thenReturn(4);

        Block overworldAnchor = block(world("world", World.Environment.NORMAL), Material.RESPAWN_ANCHOR, anchorData);
        PlayerInteractEvent overworldEvent = interaction(overworldAnchor);
        new RespawnAnchorExplosionCancelListener(List.of("world")).onAnchorUse(overworldEvent);
        assertTrue(overworldEvent.isCancelled());

        Block netherAnchor = block(world("world_nether", World.Environment.NETHER), Material.RESPAWN_ANCHOR, anchorData);
        PlayerInteractEvent netherEvent = interaction(netherAnchor);
        new RespawnAnchorExplosionCancelListener(List.of("world_nether")).onAnchorUse(netherEvent);
        assertFalse(netherEvent.isCancelled());

        Player player = mock(Player.class);
        World overworld = overworldAnchor.getWorld();
        when(player.getWorld()).thenReturn(overworld);
        PlayerInteractEvent chargingEvent = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK,
                new ItemStack(Material.GLOWSTONE), overworldAnchor, BlockFace.UP);
        new RespawnAnchorExplosionCancelListener(List.of("world")).onAnchorUse(chargingEvent);
        assertFalse(chargingEvent.isCancelled());
    }

    private PlayerInteractEvent interaction(Block block) {
        Player player = mock(Player.class);
        World world = block.getWorld();
        when(player.getWorld()).thenReturn(world);
        return new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, block, BlockFace.UP);
    }

    private Block block(World world, Material material, org.bukkit.block.data.BlockData data) {
        Block block = mock(Block.class);
        when(block.getWorld()).thenReturn(world);
        when(block.getType()).thenReturn(material);
        when(block.getBlockData()).thenReturn(data);
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
}

package me.lele.worldSafe.listener;

import me.lele.worldSafe.listener.blocks.explosioncancel.BedExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosionprevention.BedExplosionProtectionListener;
import me.lele.worldSafe.listener.blocks.other.CropTrampleProtectionListener;
import me.lele.worldSafe.listener.blocks.other.WeavingCobwebFormationPreventionListener;
import me.lele.worldSafe.listener.entities.explosioncancel.CreeperExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosionprevention.CreeperExplosionProtectionListener;
import me.lele.worldSafe.listener.entities.other.SnowGolemSnowTrailPreventionListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListenerCompatibilityTest {

    @Test
    void legacyBedNameCancelsInvalidDimensionUse() {
        World world = world("world_nether", World.Environment.NETHER);
        Block bed = block(world, Material.BED_BLOCK);
        Player player = mock(Player.class);
        when(player.getWorld()).thenReturn(world);
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, bed, BlockFace.UP);
        new BedExplosionCancelListener(Collections.singletonList("world_nether")).onPlayerInteractEvent(event);
        assertTrue(event.isCancelled());
    }

    @Test
    void oldBlockExplosionFallbackProtectsBlocksWithoutCancellingDamage() {
        World world = world("world_nether", World.Environment.NETHER);
        Block bed = block(world, Material.BED_BLOCK);
        Player player = mock(Player.class);
        when(player.getWorld()).thenReturn(world);
        BedExplosionProtectionListener listener = new BedExplosionProtectionListener(
                Collections.singletonList("world_nether"));
        listener.onBedUse(new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, bed, BlockFace.UP));
        when(bed.getType()).thenReturn(Material.AIR);
        List<Block> affected = new ArrayList<Block>(Collections.singletonList(mock(Block.class)));
        BlockExplodeEvent event = new BlockExplodeEvent(bed, affected, 1.0f);
        listener.onBedExplosion(event);
        assertFalse(event.isCancelled());
        assertTrue(event.blockList().isEmpty());
    }

    @Test
    void legacyFarmlandAndCobwebNamesAreProtected() {
        World world = world("world", World.Environment.NORMAL);
        Block soil = block(world, Material.SOIL);
        Entity entity = mock(Entity.class);
        when(entity.getWorld()).thenReturn(world);
        EntityChangeBlockEvent trample = new EntityChangeBlockEvent(entity, soil, Material.DIRT, (byte) 0);
        new CropTrampleProtectionListener(Collections.singletonList("world")).onBlockChangeByEntity(trample);
        assertTrue(trample.isCancelled());

        Block target = block(world, Material.AIR);
        BlockState web = mock(BlockState.class);
        when(web.getType()).thenReturn(Material.WEB);
        EntityBlockFormEvent form = new EntityBlockFormEvent(entity, target, web);
        new WeavingCobwebFormationPreventionListener(Collections.singletonList("world")).onCobwebForm(form);
        assertTrue(form.isCancelled());
    }

    @Test
    void legacySnowGolemNameIsProtected() {
        World world = world("world", World.Environment.NORMAL);
        Entity snowman = mock(Entity.class);
        when(snowman.getType()).thenReturn(EntityType.SNOWMAN);
        when(snowman.getWorld()).thenReturn(world);
        Block target = block(world, Material.AIR);
        BlockState snow = mock(BlockState.class);
        when(snow.getType()).thenReturn(Material.SNOW);
        EntityBlockFormEvent event = new EntityBlockFormEvent(snowman, target, snow);
        new SnowGolemSnowTrailPreventionListener(Collections.singletonList("world")).onSnowForm(event);
        assertTrue(event.isCancelled());
    }

    @Test
    void cancellationAndProtectionModesRemainDistinct() {
        World world = world("world", World.Environment.NORMAL);
        Entity creeper = mock(Entity.class);
        when(creeper.getType()).thenReturn(EntityType.CREEPER);
        when(creeper.getWorld()).thenReturn(world);

        EntityExplodeEvent cancel = new EntityExplodeEvent(creeper, new Location(world, 0, 64, 0),
                new ArrayList<Block>(), 1.0f);
        new CreeperExplosionCancelListener(Collections.singletonList("world")).onCreeperExplode(cancel);
        assertTrue(cancel.isCancelled());

        List<Block> affected = new ArrayList<Block>(Collections.singletonList(mock(Block.class)));
        EntityExplodeEvent protect = new EntityExplodeEvent(creeper, new Location(world, 0, 64, 0), affected, 1.0f);
        new CreeperExplosionProtectionListener(Collections.singletonList("world")).onCreeperExplode(protect);
        assertFalse(protect.isCancelled());
        assertTrue(protect.blockList().isEmpty());
    }

    private Block block(World world, Material material) {
        Block block = mock(Block.class);
        when(block.getWorld()).thenReturn(world);
        when(block.getType()).thenReturn(material);
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

package me.lele.worldSafe.listener.blocks.other;

import me.lele.worldSafe.listener.entities.other.SnowGolemSnowTrailPreventionListener;
import me.lele.worldSafe.listener.entities.other.WitherRoseFormationPreventionListener;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlockFormationAndIgnitionTest {

    @Test
    void preventsWeavingCobwebFormation() {
        World world = world("world");
        EntityBlockFormEvent event = formEvent(world, Material.COBWEB);

        new WeavingCobwebFormationPreventionListener(List.of("world")).onCobwebForm(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void preventsSnowTrailsAndWitherRoseFormation() {
        World world = world("world");
        Entity snowGolem = mock(Entity.class);
        when(snowGolem.getType()).thenReturn(EntityType.SNOWMAN);
        when(snowGolem.getWorld()).thenReturn(world);
        EntityBlockFormEvent snow = formEvent(world, snowGolem, Material.SNOW);
        new SnowGolemSnowTrailPreventionListener(List.of("world")).onSnowForm(snow);
        assertTrue(snow.isCancelled());

        EntityBlockFormEvent rose = formEvent(world, mock(Entity.class), Material.WITHER_ROSE);
        new WitherRoseFormationPreventionListener(List.of("world")).onWitherRoseForm(rose);
        assertTrue(rose.isCancelled());
    }

    @Test
    void preventsConfiguredIgnitionSourcesButNotPlayerIgnition() {
        World world = world("world");
        Block block = block(world, Material.AIR);
        FireIgnitionPreventionListener listener = new FireIgnitionPreventionListener(List.of("world"));

        BlockIgniteEvent fireball = new BlockIgniteEvent(block, BlockIgniteEvent.IgniteCause.FIREBALL,
                (Entity) null);
        listener.onBlockIgnite(fireball);
        assertTrue(fireball.isCancelled());

        BlockIgniteEvent flint = new BlockIgniteEvent(block, BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL,
                (Entity) null);
        listener.onBlockIgnite(flint);
        assertFalse(flint.isCancelled());
    }

    @Test
    void decoratedPotProtectionUsesRuntimeMaterialName() {
        World world = world("world");
        Block block = block(world, Material.FLOWER_POT);
        Projectile projectile = mock(Projectile.class);
        when(projectile.getWorld()).thenReturn(world);
        ProjectileHitEvent event = new ProjectileHitEvent(projectile, block);

        DecoratedPotProjectileProtectionListener listener =
                new DecoratedPotProjectileProtectionListener(List.of("world")) {
                    @Override
                    protected boolean isDecoratedPot(Block candidate) {
                        return candidate == block;
                    }
                };
        listener.onProjectileHit(event);

        assertTrue(event.isCancelled());

        EntityChangeBlockEvent changeEvent = mock(EntityChangeBlockEvent.class);
        when(changeEvent.getEntity()).thenReturn(projectile);
        when(changeEvent.getBlock()).thenReturn(block);
        listener.onProjectileChangeBlock(changeEvent);
        verify(changeEvent).setCancelled(true);
    }

    private EntityBlockFormEvent formEvent(World world, Material material) {
        Entity entity = mock(Entity.class);
        when(entity.getWorld()).thenReturn(world);
        return formEvent(world, entity, material);
    }

    private EntityBlockFormEvent formEvent(World world, Entity entity, Material material) {
        Block block = block(world, Material.AIR);
        BlockState state = mock(BlockState.class);
        when(state.getType()).thenReturn(material);
        return new EntityBlockFormEvent(entity, block, state);
    }

    private Block block(World world, Material material) {
        Block block = mock(Block.class);
        when(block.getWorld()).thenReturn(world);
        when(block.getType()).thenReturn(material);
        return block;
    }

    private World world(String name) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(name);
        return world;
    }
}

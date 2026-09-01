package me.lele.worldSafe.listener.entities.other;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WindAndBossProtectionTest {

    @Test
    void windChargeProtectionRemovesOnlyFragileBlocks() {
        World world = world("world");
        Block pot = block(world, Material.FLOWER_POT);
        Block stone = block(world, Material.STONE);
        EntityExplodeEvent event = explosion(world, EntityType.SNOWBALL, new ArrayList<>(List.of(pot, stone)));

        WindChargeBlockDestructionProtectionListener listener =
                new WindChargeBlockDestructionProtectionListener(List.of("world")) {
                    @Override
                    protected boolean isWindCharge(Entity entity) {
                        return true;
                    }

                    @Override
                    protected boolean isFragileBlock(Block block) {
                        return block == pot;
                    }
                };
        listener.onWindChargeExplosion(event);

        assertFalse(event.isCancelled());
        assertEquals(List.of(stone), event.blockList());
    }

    @Test
    void breezeImpactCancelDoesNotAffectPlayerWindCharges() {
        World world = world("world");
        BreezeWindChargeImpactCancelListener listener = new BreezeWindChargeImpactCancelListener(List.of("world")) {
            @Override
            protected boolean isBreezeWindCharge(Entity entity) {
                return entity.getType() == EntityType.SNOWBALL;
            }
        };

        EntityExplodeEvent breeze = explosion(world, EntityType.SNOWBALL, new ArrayList<>());
        listener.onBreezeWindChargeExplosion(breeze);
        assertTrue(breeze.isCancelled());

        EntityExplodeEvent player = explosion(world, EntityType.ARROW, new ArrayList<>());
        listener.onBreezeWindChargeExplosion(player);
        assertFalse(player.isCancelled());
    }

    @Test
    void breezeProjectileHitIsCancelled() {
        World world = world("world");
        Projectile projectile = mock(Projectile.class);
        when(projectile.getType()).thenReturn(EntityType.SNOWBALL);
        when(projectile.getWorld()).thenReturn(world);
        ProjectileHitEvent event = new ProjectileHitEvent(projectile, block(world, Material.STONE));

        BreezeWindChargeImpactCancelListener listener = new BreezeWindChargeImpactCancelListener(List.of("world")) {
            @Override
            protected boolean isBreezeWindCharge(Entity entity) {
                return true;
            }
        };
        listener.onBreezeWindChargeHit(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void enderDragonProtectionKeepsExplosionButClearsBlocks() {
        World world = world("world_the_end");
        EntityExplodeEvent event = explosion(world, EntityType.ENDER_DRAGON,
                new ArrayList<>(List.of(block(world, Material.END_STONE))));

        new EnderDragonBlockDestructionProtectionListener(List.of("world_the_end"))
                .onEnderDragonDestroyBlock(event);

        assertFalse(event.isCancelled());
        assertTrue(event.blockList().isEmpty());
    }

    private EntityExplodeEvent explosion(World world, EntityType type, List<Block> blocks) {
        Entity entity = mock(Entity.class);
        when(entity.getType()).thenReturn(type);
        when(entity.getWorld()).thenReturn(world);
        return new EntityExplodeEvent(entity, new Location(world, 0, 64, 0), blocks, 1.0f);
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

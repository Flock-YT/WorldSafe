package me.lele.worldSafe.listener.entities.explosion;

import me.lele.worldSafe.listener.entities.explosioncancel.CreeperExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.SulfurCubeExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosionprevention.CreeperExplosionProtectionListener;
import me.lele.worldSafe.listener.entities.explosionprevention.SulfurCubeExplosionProtectionListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExplosionListenerTest {

    @Test
    void cancelModeCancelsExplosion() {
        World world = world("world");
        EntityExplodeEvent event = explosion(world, EntityType.CREEPER, new ArrayList<>());

        new CreeperExplosionCancelListener(List.of("world")).onCreeperExplode(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void protectionModeOnlyClearsBlocks() {
        World world = world("world");
        List<Block> blocks = new ArrayList<>(List.of(mock(Block.class)));
        EntityExplodeEvent event = explosion(world, EntityType.CREEPER, blocks);

        new CreeperExplosionProtectionListener(List.of("world")).onCreeperExplode(event);

        assertFalse(event.isCancelled());
        assertEquals(0, event.blockList().size());
    }

    @Test
    void futureSulfurCubeNameWorksWithoutCompileTimeEnum() {
        World world = world("world");
        EntityExplodeEvent cancelEvent = explosion(world, EntityType.SLIME, new ArrayList<>());
        SulfurCubeExplosionCancelListener cancelListener = new SulfurCubeExplosionCancelListener(List.of("world")) {
            @Override
            protected boolean isSulfurCube(Entity entity) {
                return true;
            }
        };
        cancelListener.onSulfurCubeExplosion(cancelEvent);
        assertTrue(cancelEvent.isCancelled());

        List<Block> blocks = new ArrayList<>(List.of(mock(Block.class)));
        EntityExplodeEvent protectEvent = explosion(world, EntityType.SLIME, blocks);
        SulfurCubeExplosionProtectionListener protectionListener =
                new SulfurCubeExplosionProtectionListener(List.of("world")) {
                    @Override
                    protected boolean isSulfurCube(Entity entity) {
                        return true;
                    }
                };
        protectionListener.onSulfurCubeExplosion(protectEvent);
        assertFalse(protectEvent.isCancelled());
        assertTrue(protectEvent.blockList().isEmpty());
    }

    @Test
    void unconfiguredWorldIsUnaffected() {
        EntityExplodeEvent event = explosion(world("other"), EntityType.CREEPER,
                new ArrayList<>(List.of(mock(Block.class))));

        new CreeperExplosionCancelListener(List.of("world")).onCreeperExplode(event);

        assertFalse(event.isCancelled());
        assertEquals(1, event.blockList().size());
    }

    private EntityExplodeEvent explosion(World world, EntityType type, List<Block> blocks) {
        Entity entity = mock(Entity.class);
        when(entity.getType()).thenReturn(type);
        when(entity.getWorld()).thenReturn(world);
        return new EntityExplodeEvent(entity, new Location(world, 0, 64, 0), blocks, 1.0f);
    }

    private World world(String name) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(name);
        return world;
    }
}

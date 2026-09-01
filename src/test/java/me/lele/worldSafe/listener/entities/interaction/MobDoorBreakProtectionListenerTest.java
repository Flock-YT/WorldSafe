package me.lele.worldSafe.listener.entities.interaction;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobDoorBreakProtectionListenerTest {

    @Test
    void cancelsMobsButLeavesPlayersAlone() {
        World world = world("world");
        MobDoorBreakProtectionListener listener = new MobDoorBreakProtectionListener(List.of("world"));

        EntityBreakDoorEvent zombieEvent = event(world, EntityType.ZOMBIE);
        listener.onMobBreakDoor(zombieEvent);
        verify(zombieEvent).setCancelled(true);

        EntityBreakDoorEvent playerEvent = event(world, EntityType.PLAYER);
        listener.onMobBreakDoor(playerEvent);
        verify(playerEvent, never()).setCancelled(true);
    }

    private EntityBreakDoorEvent event(World world, EntityType type) {
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getType()).thenReturn(type);
        when(entity.getWorld()).thenReturn(world);
        Block block = mock(Block.class);
        when(block.getWorld()).thenReturn(world);
        EntityBreakDoorEvent event = mock(EntityBreakDoorEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(event.getBlock()).thenReturn(block);
        return event;
    }

    private World world(String name) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(name);
        return world;
    }
}

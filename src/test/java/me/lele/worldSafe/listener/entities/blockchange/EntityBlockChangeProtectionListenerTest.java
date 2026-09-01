package me.lele.worldSafe.listener.entities.blockchange;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntityBlockChangeProtectionListenerTest {

    @Test
    void cancelsTargetMobBlockChangesInConfiguredWorld() {
        World world = world("world");
        EntityType[] protectedTypes = {
                EntityType.RAVAGER, EntityType.SILVERFISH, EntityType.RABBIT,
                EntityType.SHEEP, EntityType.VILLAGER, EntityType.FOX
        };
        for (EntityType type : protectedTypes) {
            EntityBlockChangeProtectionListener listener =
                    new EntityBlockChangeProtectionListener(List.of("world"), type.name());
            EntityChangeBlockEvent event = event(world, type);
            listener.onEntityChangeBlock(event);
            assertTrue(event.isCancelled(), type.name());
        }

        EntityBlockChangeProtectionListener listener =
                new EntityBlockChangeProtectionListener(List.of("world"), "RAVAGER");
        EntityChangeBlockEvent pigEvent = event(world, EntityType.PIG);
        listener.onEntityChangeBlock(pigEvent);
        assertFalse(pigEvent.isCancelled());
    }

    private EntityChangeBlockEvent event(World world, EntityType type) {
        Entity entity = mock(Entity.class);
        when(entity.getType()).thenReturn(type);
        when(entity.getWorld()).thenReturn(world);
        Block block = mock(Block.class);
        when(block.getWorld()).thenReturn(world);
        BlockData data = mock(BlockData.class);
        when(data.getMaterial()).thenReturn(Material.AIR);
        return new EntityChangeBlockEvent(entity, block, data);
    }

    private World world(String name) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(name);
        return world;
    }
}

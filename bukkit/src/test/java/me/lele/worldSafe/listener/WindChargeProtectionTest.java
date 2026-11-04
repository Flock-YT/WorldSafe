package me.lele.worldSafe.listener;

import me.lele.worldSafe.listener.entities.other.WindChargeBlockDestructionProtectionListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WindChargeProtectionTest {
    @Test
    void protectsBothEventPathsOnlyForWindChargesAndConfiguredBlocks() throws Exception {
        WindChargeBlockDestructionProtectionListener listener =
                new WindChargeBlockDestructionProtectionListener(Collections.singletonList("protected"));
        Method changeHandler = Arrays.stream(listener.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(EventHandler.class)
                        && Arrays.equals(method.getParameterTypes(), new Class<?>[] {EntityChangeBlockEvent.class}))
                .findFirst().orElseThrow(() -> new AssertionError("Missing wind-charge block-change handler"));
        for (String entityName : new String[] {"WIND_CHARGE", "BREEZE_WIND_CHARGE", "ARROW"}) {
            for (String materialName : new String[] {"DECORATED_POT", "CHORUS_FLOWER", "POINTED_DRIPSTONE", "STONE"}) {
                for (String worldName : new String[] {"protected", "outside"}) {
                    World world = mock(World.class);
                    when(world.getName()).thenReturn(worldName);
                    Material material = mock(Material.class);
                    when(material.name()).thenReturn(materialName);
                    Block block = mock(Block.class);
                    when(block.getType()).thenReturn(material);
                    when(block.getWorld()).thenReturn(world);
                    EntityType type = mock(EntityType.class);
                    when(type.name()).thenReturn(entityName);
                    Entity entity = mock(Entity.class);
                    when(entity.getType()).thenReturn(type);
                    // Block location, not projectile location, controls direct-hit protection.
                    World otherWorld = mock(World.class);
                    when(otherWorld.getName()).thenReturn("outside");
                    when(entity.getWorld()).thenReturn(otherWorld);
                    boolean expected = !entityName.equals("ARROW") && !materialName.equals("STONE")
                            && worldName.equals("protected");
                    EntityChangeBlockEvent change = new EntityChangeBlockEvent(entity, block, Material.AIR, (byte) 0);
                    changeHandler.invoke(listener, change);
                    assertEquals(expected, change.isCancelled(), entityName + "/" + materialName + "/" + worldName);
                    EntityExplodeEvent explosion = new EntityExplodeEvent(entity, new Location(world, 0, 64, 0),
                            new ArrayList<Block>(Collections.singletonList(block)), 1.0f);
                    listener.onWindChargeExplosion(explosion);
                    assertEquals(expected, explosion.blockList().isEmpty());
                    assertFalse(explosion.isCancelled());
                    assertEquals(1.0f, explosion.getYield());
                }
            }
        }
    }
}

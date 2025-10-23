package me.lele.worldSafe.listener;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractWorldLimitedListenerTest {

    private static class TestListener extends AbstractWorldLimitedListener {

        protected TestListener(Collection<String> worlds) {
            super(worlds);
        }

        boolean checkWorldName(String worldName) {
            return isWorldEnabled(worldName);
        }

        boolean checkWorld(World world) {
            return isWorldEnabled(world);
        }

        boolean checkLocation(Location location) {
            return isWorldEnabled(location);
        }

        boolean checkEntity(Entity entity) {
            return isWorldEnabled(entity);
        }
    }

    @Test
    void shouldRejectNullWorldCollections() {
        TestListener listener = new TestListener(null);

        assertFalse(listener.checkWorldName("world"));
    }

    @Test
    void shouldRecogniseEnabledWorldByName() {
        TestListener listener = new TestListener(List.of("world", "nether"));

        assertTrue(listener.checkWorldName("world"));
        assertFalse(listener.checkWorldName("end"));
    }

    @Test
    void shouldHandleNullInputsGracefully() {
        TestListener listener = new TestListener(List.of("world"));

        assertFalse(listener.checkWorld(null));
        assertFalse(listener.checkLocation(null));
        assertFalse(listener.checkEntity(null));
        assertFalse(listener.checkWorldName(null));
    }

    @Test
    void shouldCheckWorldFromBukkitObjects() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");

        Location location = mock(Location.class);
        when(location.getWorld()).thenReturn(world);

        Entity entity = mock(Entity.class);
        when(entity.getWorld()).thenReturn(world);

        TestListener listener = new TestListener(List.of("world"));

        assertTrue(listener.checkWorld(world));
        assertTrue(listener.checkLocation(location));
        assertTrue(listener.checkEntity(entity));
    }
}

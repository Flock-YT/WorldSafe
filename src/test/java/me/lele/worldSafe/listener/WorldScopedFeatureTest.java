package me.lele.worldSafe.listener;

import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorldScopedFeatureTest {

    @Test
    void normalizesConfiguredWorldNames() {
        TestFeature feature = new TestFeature(List.of(" World ", "WORLD_NETHER"));
        assertTrue(feature.enabled(world("world")));
        assertTrue(feature.enabled(world("world_nether")));
        assertFalse(feature.enabled(world("other")));
        assertFalse(feature.enabled(null));
    }

    private World world(String name) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(name);
        return world;
    }

    private static final class TestFeature extends WorldScopedFeature {
        private TestFeature(List<String> worlds) {
            super(worlds);
        }

        private boolean enabled(World world) {
            return isWorldEnabled(world);
        }
    }
}

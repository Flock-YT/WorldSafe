package me.lele.worldSafe.feature;

import me.lele.worldSafe.compat.MinecraftVersion;
import me.lele.worldSafe.compat.ServerCapabilities;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureDefinitionTest {

    @Test
    void reportsMinimumVersionBeforeCapabilityFailures() {
        FeatureDefinition definition = definition();
        String reason = definition.getUnsupportedReason(new MinecraftVersion(1, 15, 2), emptyCapabilities());
        assertTrue(reason.contains("requires Minecraft 1.16+"));
        assertTrue(reason.contains("detected 1.15.2"));
    }

    @Test
    void reportsMissingRuntimeCapabilityAndAcceptsCompleteSnapshot() {
        FeatureDefinition definition = definition();
        String reason = definition.getUnsupportedReason(MinecraftVersion.V1_16, emptyCapabilities());
        assertTrue(reason.contains("RESPAWN_ANCHOR_CHARGES"));

        ServerCapabilities complete = ServerCapabilities.forTesting(
                EnumSet.of(ServerCapabilities.Capability.RESPAWN_ANCHOR_CHARGES),
                Collections.singleton("RESPAWN_ANCHOR"), Collections.<String>emptySet());
        assertNull(definition.getUnsupportedReason(MinecraftVersion.V1_16, complete));
    }

    private FeatureDefinition definition() {
        return new FeatureDefinition("respawnAnchorExplosionCancel", MinecraftVersion.V1_16,
                new ServerCapabilities.Capability[] {ServerCapabilities.Capability.RESPAWN_ANCHOR_CHARGES},
                new String[] {"RESPAWN_ANCHOR"}, new String[0],
                (worlds, capabilities) -> new Listener() { });
    }

    private ServerCapabilities emptyCapabilities() {
        return ServerCapabilities.forTesting(Collections.<ServerCapabilities.Capability>emptySet(),
                Collections.<String>emptySet(), Collections.<String>emptySet());
    }
}

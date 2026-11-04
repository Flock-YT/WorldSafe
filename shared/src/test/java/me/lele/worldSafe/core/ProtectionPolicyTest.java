package me.lele.worldSafe.core;

import me.lele.worldSafe.compat.MinecraftVersion;
import me.lele.worldSafe.config.WorldSafeConfig;
import me.lele.worldSafe.core.ConfigValidator;
import me.lele.worldSafe.core.FeatureCatalog;
import me.lele.worldSafe.core.ProtectionPolicy;
import me.lele.worldSafe.core.WorldSelector;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ProtectionPolicyTest {
    @Test
    void fabricDimensionSyntaxAcceptsOnlyNativeIdsAndTheThreeLegacyAliases() {
        for (String valid : Arrays.asList("world", "world_nether", "world_the_end", "minecraft:overworld", "example:path/moon")) {
            assertTrue(WorldSelector.isFabricDimension(valid), valid);
        }
        for (String invalid : Arrays.asList("", "my_world", "missing:", ":missing", "example:bad value")) {
            assertFalse(WorldSelector.isFabricDimension(invalid), invalid);
        }
        assertFalse(WorldSelector.isFabricDimension(null));
    }

    @Test
    void legacyAliasesAndNativeDimensionsResolveToTheSameProtectedWorld() {
        Map<String, java.util.List<String>> worlds = new LinkedHashMap<String, java.util.List<String>>();
        worlds.put("tntExplosionProtection", Arrays.asList(" World_Nether ", "example:moon"));
        ProtectionPolicy policy = new ProtectionPolicy(new WorldSafeConfig(true, false, worlds),
                MinecraftVersion.V1_14, WorldSelector::fabricDimension);
        assertTrue(policy.enabled("tntExplosionProtection", "minecraft:the_nether"));
        assertTrue(policy.enabled("tntExplosionProtection", "example:moon"));
        assertFalse(policy.enabled("tntExplosionProtection", "minecraft:overworld"));
        assertFalse(policy.enabled("creeperExplosionProtection", "minecraft:the_nether"));
    }

    @Test
    void fullCancellationWinsWithoutChangingTheSnapshot() {
        Map<String, java.util.List<String>> worlds = new LinkedHashMap<String, java.util.List<String>>();
        worlds.put("tntExplosionProtection", Collections.singletonList("world"));
        worlds.put("tntExplosionCancel", Collections.singletonList("world"));
        WorldSafeConfig config = new WorldSafeConfig(true, false, worlds);
        ProtectionPolicy policy = new ProtectionPolicy(config, MinecraftVersion.V1_14, WorldSelector::normalize);
        worlds.clear();
        assertEquals(ProtectionPolicy.ExplosionAction.CANCEL,
                policy.explosion("tntExplosionCancel", "tntExplosionProtection", "world"));
        assertEquals(ProtectionPolicy.ExplosionAction.NORMAL,
                policy.explosion("tntExplosionCancel", "tntExplosionProtection", "other"));
    }

    @Test
    void absentGameFeaturesAndTheMainSwitchRemainDisabled() {
        Map<String, java.util.List<String>> worlds = Collections.singletonMap(
                "respawnAnchorExplosionCancel", Collections.singletonList("world"));
        assertFalse(new ProtectionPolicy(new WorldSafeConfig(true, false, worlds),
                MinecraftVersion.V1_14, WorldSelector::normalize)
                .enabled("respawnAnchorExplosionCancel", "world"));
        assertFalse(new ProtectionPolicy(new WorldSafeConfig(false, false, worlds),
                MinecraftVersion.V1_16, WorldSelector::normalize)
                .enabled("respawnAnchorExplosionCancel", "world"));
    }

    @Test
    void sharedSchemaRejectsInvalidKeysAndNonStringWorlds() {
        Map<String, Object> raw = new LinkedHashMap<String, Object>();
        raw.put("tntExplosionProtection", Arrays.asList("world", 42));
        assertNull(ConfigValidator.parse(raw, FeatureCatalog.all().keySet(), message -> { }, message -> { }));
        raw.clear();
        raw.put("unknownFeature", Collections.emptyList());
        assertNull(ConfigValidator.parse(raw, FeatureCatalog.all().keySet(), message -> { }, message -> { }));
    }
}

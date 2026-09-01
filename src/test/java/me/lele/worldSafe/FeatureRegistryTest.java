package me.lele.worldSafe;

import me.lele.worldSafe.compat.MinecraftVersion;
import me.lele.worldSafe.compat.ServerCapabilities;
import me.lele.worldSafe.feature.FeatureDefinition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureRegistryTest {

    @Test
    void everyFeatureUsesItsDocumentedMinimumVersion() {
        Map<String, MinecraftVersion> expected = new LinkedHashMap<String, MinecraftVersion>();
        add(expected, MinecraftVersion.V1_8_8,
                "bedExplosionCancel", "tntExplosionCancel", "bedExplosionProtection", "tntExplosionProtection",
                "creeperExplosionCancel", "endCrystalExplosionCancel", "ghastExplosionCancel",
                "witherExplosionCancel", "creeperExplosionProtection", "endCrystalExplosionPrevention",
                "ghastExplosionProtection", "witherExplosionProtection", "cropTrampleProtection",
                "dragonEggTeleportationPrevention", "fireSpreadPrevention", "fireIgnitionPrevention",
                "enderDragonBlockDestructionProtection", "enderManBlockPickupProtection",
                "silverfishBlockChangeProtection", "rabbitCropEatingProtection", "sheepGrassEatingProtection",
                "villagerCropModificationProtection", "mobDoorBreakProtection", "snowGolemSnowTrailPrevention");
        add(expected, MinecraftVersion.V1_13, "phantomDamagePrevention");
        add(expected, MinecraftVersion.V1_14, "ravagerBlockDestructionProtection",
                "foxBerryHarvestProtection", "witherRoseFormationPrevention");
        add(expected, MinecraftVersion.V1_16, "respawnAnchorExplosionCancel",
                "respawnAnchorExplosionPrevention");
        add(expected, MinecraftVersion.V1_20_3, "decoratedPotProjectileProtection");
        add(expected, MinecraftVersion.V1_21, "windChargeBlockDestructionProtection",
                "breezeWindChargeImpactCancel", "weavingCobwebFormationPrevention");
        add(expected, MinecraftVersion.V26_2, "sulfurCubeExplosionCancel", "sulfurCubeExplosionProtection");

        assertEquals(expected.size(), WorldSafe.FEATURES.size());
        for (FeatureDefinition feature : WorldSafe.FEATURES) {
            assertEquals(expected.get(feature.getConfigKey()), feature.getMinimumVersion(), feature.getConfigKey());
        }
    }

    @Test
    void everyFeaturePassesAtItsMinimumAndFailsBeforeIt() {
        Set<String> names = new LinkedHashSet<String>(Arrays.asList(
                "BED_BLOCK", "RED_BED", "RESPAWN_ANCHOR", "DECORATED_POT", "COBWEB", "WITHER_ROSE",
                "PRIMED_TNT", "TNT", "CREEPER", "ENDER_CRYSTAL", "END_CRYSTAL", "GHAST", "WITHER",
                "ENDER_DRAGON", "ENDERMAN", "SILVERFISH", "RABBIT", "SHEEP", "VILLAGER", "SNOWMAN",
                "SNOW_GOLEM", "PHANTOM", "RAVAGER", "FOX", "WIND_CHARGE", "BREEZE_WIND_CHARGE",
                "SULFUR_CUBE", "SULPHUR_CUBE"));
        ServerCapabilities capabilities = ServerCapabilities.forTesting(
                EnumSet.allOf(ServerCapabilities.Capability.class), names, names);

        for (FeatureDefinition feature : WorldSafe.FEATURES) {
            assertNull(feature.getUnsupportedReason(feature.getMinimumVersion(), capabilities),
                    feature.getConfigKey());
            MinecraftVersion earlier = earlierThan(feature.getMinimumVersion());
            String reason = feature.getUnsupportedReason(earlier, capabilities);
            assertTrue(reason != null && reason.contains("requires Minecraft"), feature.getConfigKey());
        }
    }

    private void add(Map<String, MinecraftVersion> versions, MinecraftVersion version, String... keys) {
        for (String key : keys) {
            versions.put(key, version);
        }
    }

    private MinecraftVersion earlierThan(MinecraftVersion version) {
        if (MinecraftVersion.V1_8_8.equals(version)) {
            return new MinecraftVersion(1, 8, 7);
        }
        if (MinecraftVersion.V1_13.equals(version)) {
            return new MinecraftVersion(1, 12, 2);
        }
        if (MinecraftVersion.V1_14.equals(version)) {
            return new MinecraftVersion(1, 13, 2);
        }
        if (MinecraftVersion.V1_16.equals(version)) {
            return new MinecraftVersion(1, 15, 2);
        }
        if (MinecraftVersion.V1_20_3.equals(version)) {
            return new MinecraftVersion(1, 20, 2);
        }
        if (MinecraftVersion.V1_21.equals(version)) {
            return new MinecraftVersion(1, 20, 6);
        }
        return new MinecraftVersion(26, 1, 2);
    }
}

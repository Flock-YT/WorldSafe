package me.lele.worldSafe.core;

import me.lele.worldSafe.compat.MinecraftVersion;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Platform-independent keys and minimum versions for all supported protections. */
public final class FeatureCatalog {
    private static final Map<String, FeatureSpec> FEATURES;
    static {
        Map<String, FeatureSpec> features = new LinkedHashMap<String, FeatureSpec>();
        add(features, "bedExplosionCancel", MinecraftVersion.V1_8_8);
        add(features, "tntExplosionCancel", MinecraftVersion.V1_8_8);
        add(features, "bedExplosionProtection", MinecraftVersion.V1_8_8);
        add(features, "tntExplosionProtection", MinecraftVersion.V1_8_8);
        add(features, "creeperExplosionCancel", MinecraftVersion.V1_8_8);
        add(features, "endCrystalExplosionCancel", MinecraftVersion.V1_8_8);
        add(features, "ghastExplosionCancel", MinecraftVersion.V1_8_8);
        add(features, "witherExplosionCancel", MinecraftVersion.V1_8_8);
        add(features, "creeperExplosionProtection", MinecraftVersion.V1_8_8);
        add(features, "endCrystalExplosionPrevention", MinecraftVersion.V1_8_8);
        add(features, "ghastExplosionProtection", MinecraftVersion.V1_8_8);
        add(features, "witherExplosionProtection", MinecraftVersion.V1_8_8);
        add(features, "cropTrampleProtection", MinecraftVersion.V1_8_8);
        add(features, "dragonEggTeleportationPrevention", MinecraftVersion.V1_8_8);
        add(features, "fireSpreadPrevention", MinecraftVersion.V1_8_8);
        add(features, "fireIgnitionPrevention", MinecraftVersion.V1_8_8);
        add(features, "enderDragonBlockDestructionProtection", MinecraftVersion.V1_8_8);
        add(features, "enderManBlockPickupProtection", MinecraftVersion.V1_8_8);
        add(features, "silverfishBlockChangeProtection", MinecraftVersion.V1_8_8);
        add(features, "rabbitCropEatingProtection", MinecraftVersion.V1_8_8);
        add(features, "sheepGrassEatingProtection", MinecraftVersion.V1_8_8);
        add(features, "villagerCropModificationProtection", MinecraftVersion.V1_8_8);
        add(features, "mobDoorBreakProtection", MinecraftVersion.V1_8_8);
        add(features, "snowGolemSnowTrailPrevention", MinecraftVersion.V1_8_8);
        add(features, "phantomDamagePrevention", MinecraftVersion.V1_13);
        add(features, "ravagerBlockDestructionProtection", MinecraftVersion.V1_14);
        add(features, "foxBerryHarvestProtection", MinecraftVersion.V1_14);
        add(features, "witherRoseFormationPrevention", MinecraftVersion.V1_14);
        add(features, "respawnAnchorExplosionCancel", MinecraftVersion.V1_16);
        add(features, "respawnAnchorExplosionPrevention", MinecraftVersion.V1_16);
        add(features, "decoratedPotProjectileProtection", MinecraftVersion.V1_20_3);
        add(features, "windChargeBlockDestructionProtection", MinecraftVersion.V1_21);
        add(features, "breezeWindChargeImpactCancel", MinecraftVersion.V1_21);
        add(features, "weavingCobwebFormationPrevention", MinecraftVersion.V1_21);
        add(features, "sulfurCubeExplosionCancel", MinecraftVersion.V26_2);
        add(features, "sulfurCubeExplosionProtection", MinecraftVersion.V26_2);
        FEATURES = Collections.unmodifiableMap(features);
    }

    private FeatureCatalog() { }

    private static void add(Map<String, FeatureSpec> features, String key, MinecraftVersion minimum) {
        if (features.put(key, new FeatureSpec(key, minimum)) != null) {
            throw new IllegalStateException("Duplicate feature: " + key);
        }
    }

    public static Map<String, FeatureSpec> all() { return FEATURES; }

    public static FeatureSpec get(String key) {
        FeatureSpec spec = FEATURES.get(key);
        if (spec == null) { throw new IllegalArgumentException("Unknown protection: " + key); }
        return spec;
    }
}

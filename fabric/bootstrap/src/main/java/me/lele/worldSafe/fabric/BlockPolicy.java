package me.lele.worldSafe.fabric;

/** Native callbacks establish short, exception-safe contexts around vanilla work. */
public final class BlockPolicy {
    private static final ThreadLocal<Object> ACTOR = new ThreadLocal<Object>();
    private static final ThreadLocal<String> REASON = new ThreadLocal<String>();

    private BlockPolicy() { }

    public static Object pushActor(Object actor) {
        Object previous = ACTOR.get();
        ACTOR.set(actor);
        return previous;
    }

    public static void restoreActor(Object previous) {
        if (previous == null) { ACTOR.remove(); } else { ACTOR.set(previous); }
    }

    public static String pushReason(String reason) {
        String previous = REASON.get();
        REASON.set(reason);
        return previous;
    }

    public static void restoreReason(String previous) {
        if (previous == null) { REASON.remove(); } else { REASON.set(previous); }
    }

    public static Object entityWorld(Object actor) {
        return actor instanceof NativeEntityAccess ? ((NativeEntityAccess) actor).worldsafe$getWorld() : null;
    }

    /** Cancel the whole burn operation: vanilla can prime TNT even when its block write fails. */
    public static boolean cancelFireBurn(Object world) {
        return FabricServices.enabled("fireSpreadPrevention", world);
    }

    public static boolean blockChange(Object world, Object position, Object nextState, Object explicitActor) {
        Object actor = explicitActor == null ? ACTOR.get() : explicitActor;
        if (actor == null && REASON.get() == null) { return false; }
        if (FabricServices.policy(world) == null) { return false; }
        NativeAccess access = FabricServices.nativeAccess();
        String before = access.blockType(world, position);
        String after = access.stateType(nextState);
        String type = access.entityType(actor);
        boolean air = "minecraft:air".equals(after) || "minecraft:cave_air".equals(after)
                || "minecraft:void_air".equals(after);
        if ("minecraft:decorated_pot".equals(before)
                && FabricServices.enabled("decoratedPotProjectileProtection", world)
                && access.isProjectile(actor)) { return true; }
        if ("minecraft:farmland".equals(before) && ("minecraft:dirt".equals(after)
                || "minecraft:grass_block".equals(after)) && actor != null
                && FabricServices.enabled("cropTrampleProtection", world)) { return true; }
        if ("minecraft:enderman".equals(type)
                && FabricServices.enabled("enderManBlockPickupProtection", world)) { return true; }
        String feature = null;
        if ("minecraft:silverfish".equals(type) && (before.startsWith("minecraft:infested_") || after.startsWith("minecraft:infested_"))) { feature = "silverfishBlockChangeProtection"; }
        else if ("minecraft:rabbit".equals(type) && "minecraft:carrots".equals(before)) { feature = "rabbitCropEatingProtection"; }
        else if ("minecraft:sheep".equals(type) && ("minecraft:grass_block".equals(before) || before.endsWith("grass"))) { feature = "sheepGrassEatingProtection"; }
        else if ("minecraft:villager".equals(type) && (crop(before) || crop(after))) { feature = "villagerCropModificationProtection"; }
        else if ("minecraft:ravager".equals(type) && (before.endsWith("_leaves") || crop(before))) { feature = "ravagerBlockDestructionProtection"; }
        else if ("minecraft:fox".equals(type) && ("minecraft:sweet_berry_bush".equals(before) || before.startsWith("minecraft:cave_vines"))) { feature = "foxBerryHarvestProtection"; }
        else if ("minecraft:ender_dragon".equals(type) && "dragon".equals(REASON.get()) && air) { feature = "enderDragonBlockDestructionProtection"; }
        else if (("minecraft:wither".equals(type) || "minecraft:wither_skull".equals(type)) && air) {
            feature = "witherExplosionProtection";
        }
        if (feature != null && FabricServices.enabled(feature, world)) { return true; }
        if (air && before.endsWith("_door") && actor != null && !"minecraft:player".equals(type)
                && FabricServices.enabled("mobDoorBreakProtection", world)) { return true; }
        if ("minecraft:snow_golem".equals(type) && "minecraft:snow".equals(after)
                && FabricServices.enabled("snowGolemSnowTrailPrevention", world)) { return true; }
        boolean fire = "minecraft:fire".equals(after) || "minecraft:soul_fire".equals(after);
        String reason = REASON.get();
        if (FabricServices.enabled("fireSpreadPrevention", world)) {
            if (("fire".equals(reason) || "lava".equals(reason)) && fire && !before.equals(after)) { return true; }
            if ("fire".equals(reason) && air && !"minecraft:fire".equals(before)
                    && !"minecraft:soul_fire".equals(before) && !"minecraft:air".equals(before)) { return true; }
        }
        if (fire && !before.equals(after) && FabricServices.enabled("fireIgnitionPrevention", world)
                && ("minecraft:lightning_bolt".equals(type) || "minecraft:fireball".equals(type)
                    || "minecraft:small_fireball".equals(type) || "minecraft:end_crystal".equals(type)
                    || "minecraft:arrow".equals(type) || "minecraft:spectral_arrow".equals(type))) { return true; }
        return ExplosionPolicy.protectWindChargeBlock(world, actor, position);
    }

    private static boolean crop(String block) {
        return "minecraft:wheat".equals(block) || "minecraft:carrots".equals(block)
                || "minecraft:potatoes".equals(block) || "minecraft:beetroots".equals(block)
                || "minecraft:torchflower_crop".equals(block) || "minecraft:pitcher_crop".equals(block);
    }

    public static boolean cancelProjectileBlock(Object world, Object projectile, boolean decoratedPot, boolean campfire) {
        if (decoratedPot && FabricServices.enabled("decoratedPotProjectileProtection", world)) { return true; }
        if (campfire && FabricServices.enabled("fireIgnitionPrevention", world)) { return true; }
        String type = FabricServices.nativeAccess().entityType(projectile);
        if ("minecraft:breeze_wind_charge".equals(type)
                && FabricServices.enabled("breezeWindChargeImpactCancel", world)) { return true; }
        return !campfire && ("minecraft:wind_charge".equals(type) || "minecraft:breeze_wind_charge".equals(type))
                && FabricServices.enabled("windChargeBlockDestructionProtection", world);
    }
}

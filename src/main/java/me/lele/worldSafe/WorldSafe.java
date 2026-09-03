package me.lele.worldSafe;

import me.lele.worldSafe.command.WorldSafeCommand;
import me.lele.worldSafe.compat.MinecraftVersion;
import me.lele.worldSafe.compat.ServerCapabilities;
import me.lele.worldSafe.config.ConfigManager;
import me.lele.worldSafe.config.WorldSafeConfig;
import me.lele.worldSafe.feature.FeatureDefinition;
import me.lele.worldSafe.listener.ListenerManager;
import me.lele.worldSafe.listener.blocks.explosioncancel.BedExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosioncancel.RespawnAnchorExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosioncancel.TNTExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosionprevention.BedExplosionProtectionListener;
import me.lele.worldSafe.listener.blocks.explosionprevention.RespawnAnchorExplosionPreventionListener;
import me.lele.worldSafe.listener.blocks.explosionprevention.TNTExplosionProtectionListener;
import me.lele.worldSafe.listener.blocks.other.CropTrampleProtectionListener;
import me.lele.worldSafe.listener.blocks.other.DecoratedPotProjectileProtectionListener;
import me.lele.worldSafe.listener.blocks.other.DragonEggTeleportationPreventionListener;
import me.lele.worldSafe.listener.blocks.other.FireIgnitionPreventionListener;
import me.lele.worldSafe.listener.blocks.other.FireSpreadPreventionListener;
import me.lele.worldSafe.listener.blocks.other.WeavingCobwebFormationPreventionListener;
import me.lele.worldSafe.listener.entities.blockchange.EntityBlockChangeProtectionListener;
import me.lele.worldSafe.listener.entities.explosioncancel.CreeperExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.EndCrystalExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.GhastExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.SulfurCubeExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.WitherExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosionprevention.CreeperExplosionProtectionListener;
import me.lele.worldSafe.listener.entities.explosionprevention.EndCrystalExplosionPreventionListener;
import me.lele.worldSafe.listener.entities.explosionprevention.GhastExplosionProtectionListener;
import me.lele.worldSafe.listener.entities.explosionprevention.SulfurCubeExplosionProtectionListener;
import me.lele.worldSafe.listener.entities.explosionprevention.WitherExplosionProtectionListener;
import me.lele.worldSafe.listener.entities.interaction.MobDoorBreakProtectionListener;
import me.lele.worldSafe.listener.entities.other.BreezeWindChargeImpactCancelListener;
import me.lele.worldSafe.listener.entities.other.EnderDragonBlockDestructionProtectionListener;
import me.lele.worldSafe.listener.entities.other.EnderManBlockPickupProtectionListener;
import me.lele.worldSafe.listener.entities.other.PhantomDamagePreventionListener;
import me.lele.worldSafe.listener.entities.other.SnowGolemSnowTrailPreventionListener;
import me.lele.worldSafe.listener.entities.other.WindChargeBlockDestructionProtectionListener;
import me.lele.worldSafe.listener.entities.other.WitherRoseFormationPreventionListener;
import me.lele.worldSafe.metrics.MetricsManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class WorldSafe extends JavaPlugin {

    private static final ServerCapabilities.Capability[] NO_CAPABILITIES =
            new ServerCapabilities.Capability[0];
    private static final String[] NO_NAMES = new String[0];

    public static final List<FeatureDefinition> FEATURES = Collections.unmodifiableList(Arrays.asList(
            feature("bedExplosionCancel", MinecraftVersion.V1_8_8, NO_CAPABILITIES,
                    new String[] {"BED_BLOCK", "LEGACY_BED", "RED_BED"}, NO_NAMES,
                    (worlds, capabilities) -> new BedExplosionCancelListener(worlds, capabilities)),
            feature("tntExplosionCancel", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"PRIMED_TNT", "TNT"},
                    (worlds, capabilities) -> new TNTExplosionCancelListener(worlds)),
            feature("bedExplosionProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES,
                    new String[] {"BED_BLOCK", "LEGACY_BED", "RED_BED"}, NO_NAMES,
                    (worlds, capabilities) -> new BedExplosionProtectionListener(worlds, capabilities)),
            feature("tntExplosionProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"PRIMED_TNT", "TNT"},
                    (worlds, capabilities) -> new TNTExplosionProtectionListener(worlds)),
            feature("creeperExplosionCancel", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"CREEPER"}, (worlds, capabilities) -> new CreeperExplosionCancelListener(worlds)),
            feature("endCrystalExplosionCancel", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"ENDER_CRYSTAL", "END_CRYSTAL"},
                    (worlds, capabilities) -> new EndCrystalExplosionCancelListener(worlds)),
            feature("ghastExplosionCancel", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"GHAST"}, (worlds, capabilities) -> new GhastExplosionCancelListener(worlds)),
            feature("witherExplosionCancel", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"WITHER"}, (worlds, capabilities) -> new WitherExplosionCancelListener(worlds)),
            feature("creeperExplosionProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"CREEPER"},
                    (worlds, capabilities) -> new CreeperExplosionProtectionListener(worlds)),
            feature("endCrystalExplosionPrevention", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"ENDER_CRYSTAL", "END_CRYSTAL"},
                    (worlds, capabilities) -> new EndCrystalExplosionPreventionListener(worlds)),
            feature("ghastExplosionProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"GHAST"}, (worlds, capabilities) -> new GhastExplosionProtectionListener(worlds)),
            feature("witherExplosionProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES, NO_NAMES,
                    new String[] {"WITHER"}, (worlds, capabilities) -> new WitherExplosionProtectionListener(worlds)),
            simple("cropTrampleProtection", MinecraftVersion.V1_8_8,
                    (worlds, capabilities) -> new CropTrampleProtectionListener(worlds)),
            simple("dragonEggTeleportationPrevention", MinecraftVersion.V1_8_8,
                    (worlds, capabilities) -> new DragonEggTeleportationPreventionListener(worlds)),
            simple("fireSpreadPrevention", MinecraftVersion.V1_8_8,
                    (worlds, capabilities) -> new FireSpreadPreventionListener(worlds)),
            simple("fireIgnitionPrevention", MinecraftVersion.V1_8_8,
                    (worlds, capabilities) -> new FireIgnitionPreventionListener(worlds)),
            feature("enderDragonBlockDestructionProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"ENDER_DRAGON"},
                    (worlds, capabilities) -> new EnderDragonBlockDestructionProtectionListener(worlds)),
            feature("enderManBlockPickupProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"ENDERMAN"},
                    (worlds, capabilities) -> new EnderManBlockPickupProtectionListener(worlds)),
            feature("silverfishBlockChangeProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"SILVERFISH"},
                    entityBlockChangeFactory("SILVERFISH")),
            feature("rabbitCropEatingProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"RABBIT"}, entityBlockChangeFactory("RABBIT")),
            feature("sheepGrassEatingProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"SHEEP"}, entityBlockChangeFactory("SHEEP")),
            feature("villagerCropModificationProtection", MinecraftVersion.V1_8_8, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"VILLAGER"}, entityBlockChangeFactory("VILLAGER")),
            simple("mobDoorBreakProtection", MinecraftVersion.V1_8_8,
                    (worlds, capabilities) -> new MobDoorBreakProtectionListener(worlds)),
            feature("snowGolemSnowTrailPrevention", MinecraftVersion.V1_8_8, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"SNOWMAN", "SNOW_GOLEM"},
                    (worlds, capabilities) -> new SnowGolemSnowTrailPreventionListener(worlds)),
            feature("phantomDamagePrevention", MinecraftVersion.V1_13, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"PHANTOM"},
                    (worlds, capabilities) -> new PhantomDamagePreventionListener(worlds)),
            feature("ravagerBlockDestructionProtection", MinecraftVersion.V1_14, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"RAVAGER"}, entityBlockChangeFactory("RAVAGER")),
            feature("foxBerryHarvestProtection", MinecraftVersion.V1_14, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"FOX"}, entityBlockChangeFactory("FOX")),
            feature("witherRoseFormationPrevention", MinecraftVersion.V1_14, NO_CAPABILITIES,
                    new String[] {"WITHER_ROSE"}, NO_NAMES,
                    (worlds, capabilities) -> new WitherRoseFormationPreventionListener(worlds)),
            feature("respawnAnchorExplosionCancel", MinecraftVersion.V1_16,
                    new ServerCapabilities.Capability[] {ServerCapabilities.Capability.RESPAWN_ANCHOR_CHARGES},
                    new String[] {"RESPAWN_ANCHOR"}, NO_NAMES,
                    (worlds, capabilities) -> new RespawnAnchorExplosionCancelListener(worlds, capabilities)),
            feature("respawnAnchorExplosionPrevention", MinecraftVersion.V1_16,
                    new ServerCapabilities.Capability[] {ServerCapabilities.Capability.RESPAWN_ANCHOR_CHARGES},
                    new String[] {"RESPAWN_ANCHOR"}, NO_NAMES,
                    (worlds, capabilities) -> new RespawnAnchorExplosionPreventionListener(worlds, capabilities)),
            feature("decoratedPotProjectileProtection", MinecraftVersion.V1_20_3,
                    new ServerCapabilities.Capability[] {ServerCapabilities.Capability.PROJECTILE_HIT_BLOCK},
                    new String[] {"DECORATED_POT"}, NO_NAMES,
                    (worlds, capabilities) -> new DecoratedPotProjectileProtectionListener(worlds, capabilities)),
            feature("windChargeBlockDestructionProtection", MinecraftVersion.V1_21, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"WIND_CHARGE", "BREEZE_WIND_CHARGE"},
                    (worlds, capabilities) -> new WindChargeBlockDestructionProtectionListener(worlds)),
            feature("breezeWindChargeImpactCancel", MinecraftVersion.V1_21, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"BREEZE_WIND_CHARGE"},
                    (worlds, capabilities) -> new BreezeWindChargeImpactCancelListener(worlds, capabilities)),
            feature("weavingCobwebFormationPrevention", MinecraftVersion.V1_21, NO_CAPABILITIES,
                    new String[] {"WEB", "COBWEB"}, NO_NAMES,
                    (worlds, capabilities) -> new WeavingCobwebFormationPreventionListener(worlds)),
            feature("sulfurCubeExplosionCancel", MinecraftVersion.V26_2, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"SULFUR_CUBE", "SULPHUR_CUBE"},
                    (worlds, capabilities) -> new SulfurCubeExplosionCancelListener(worlds)),
            feature("sulfurCubeExplosionProtection", MinecraftVersion.V26_2, NO_CAPABILITIES,
                    NO_NAMES, new String[] {"SULFUR_CUBE", "SULPHUR_CUBE"},
                    (worlds, capabilities) -> new SulfurCubeExplosionProtectionListener(worlds))
    ));

    private final Set<String> warnedBestEffortFeatures = new LinkedHashSet<String>();
    private ConfigManager configManager;
    private ListenerManager listenerManager;
    private MetricsManager metricsManager;
    private ServerCapabilities capabilities;
    private MinecraftVersion minecraftVersion;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            minecraftVersion = MinecraftVersion.parse(getServer().getBukkitVersion());
        } catch (IllegalArgumentException exception) {
            getLogger().severe(exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        capabilities = ServerCapabilities.detect((capability, failure) ->
                getLogger().warning("Compatibility capability " + capability.name()
                        + " failed via reflection and will continue in fallback mode: "
                        + failure.getClass().getSimpleName()));
        configManager = new ConfigManager(new File(getDataFolder(), "config.yml"), getLogger());
        listenerManager = new ListenerManager(
                listener -> getServer().getPluginManager().registerEvents(listener, this),
                HandlerList::unregisterAll,
                message -> getLogger().severe(message));
        metricsManager = new MetricsManager(() -> new Metrics(this, 22831),
                message -> getLogger().warning(message));

        WorldSafeConfig initialConfig = configManager.loadCandidate(featureKeys());
        if (initialConfig == null || !applyConfiguration(initialConfig)) {
            getLogger().severe("WorldSafe could not load a valid configuration and will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginCommand command = getCommand("worldsafe");
        if (command == null) {
            getLogger().severe("Command 'worldsafe' is missing from plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        WorldSafeCommand commandHandler = new WorldSafeCommand(this);
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        getLogger().info("WorldSafe enabled for Minecraft " + minecraftVersion + ".");
    }

    @Override
    public void onDisable() {
        if (metricsManager != null) {
            metricsManager.shutdown();
        }
        if (listenerManager != null) {
            listenerManager.clear();
        }
        HandlerList.unregisterAll(this);
    }

    public boolean reloadWorldSafe() {
        WorldSafeConfig candidate = configManager.loadCandidate(featureKeys());
        if (candidate == null) {
            getLogger().severe("Reload failed; the previous configuration, listeners, and bStats state remain active.");
            return false;
        }
        if (!applyConfiguration(candidate)) {
            getLogger().severe("Reload failed; the previous configuration, listeners, and bStats state remain active.");
            return false;
        }
        return true;
    }

    private boolean applyConfiguration(final WorldSafeConfig candidate) {
        List<Listener> replacements = createListeners(candidate);
        if (replacements == null) {
            return false;
        }
        if (!listenerManager.replace(replacements, () -> configManager.commit(candidate))) {
            return false;
        }
        metricsManager.sync(candidate.isBStatsEnabled());
        return true;
    }

    private List<Listener> createListeners(WorldSafeConfig config) {
        List<Listener> created = new ArrayList<Listener>();
        if (!config.isEnabled()) {
            getLogger().info("WorldSafe is disabled in config.yml; no protection listeners were loaded.");
            return created;
        }

        for (FeatureDefinition feature : FEATURES) {
            List<String> worlds = config.getWorlds(feature.getConfigKey());
            if (worlds.isEmpty()) {
                continue;
            }
            String unsupportedReason = feature.getUnsupportedReason(minecraftVersion, capabilities);
            if (unsupportedReason != null) {
                getLogger().warning("Skipping feature '" + feature.getConfigKey() + "': " + unsupportedReason);
                continue;
            }
            warnAboutBestEffortCancellation(feature.getConfigKey());
            try {
                created.add(feature.createListener(worlds, capabilities));
            } catch (RuntimeException exception) {
                getLogger().severe("Failed to create feature '" + feature.getConfigKey() + "': "
                        + exception.getMessage());
                return null;
            } catch (LinkageError error) {
                getLogger().severe("Failed to link feature '" + feature.getConfigKey() + "': "
                        + error.getClass().getSimpleName());
                return null;
            }
        }
        return created;
    }

    private void warnAboutBestEffortCancellation(String featureKey) {
        if (capabilities.isPaperServer()) {
            return;
        }
        if (!"breezeWindChargeImpactCancel".equals(featureKey)
                && !"sulfurCubeExplosionCancel".equals(featureKey)) {
            return;
        }
        if (warnedBestEffortFeatures.add(featureKey)) {
            getLogger().warning("IMPORTANT: Feature '" + featureKey + "' is running in best-effort mode on "
                    + "a non-Paper server; explosion damage or knockback may remain because Spigot does not "
                    + "expose the required early explosion hook.");
        }
    }

    private static List<String> featureKeys() {
        List<String> keys = new ArrayList<String>();
        for (FeatureDefinition feature : FEATURES) {
            keys.add(feature.getConfigKey());
        }
        return keys;
    }

    public List<Listener> getListeners() {
        return listenerManager == null
                ? Collections.<Listener>emptyList()
                : listenerManager.getListeners();
    }

    private static FeatureDefinition simple(String key, MinecraftVersion minimumVersion,
            FeatureDefinition.ListenerFactory factory) {
        return feature(key, minimumVersion, NO_CAPABILITIES, NO_NAMES, NO_NAMES, factory);
    }

    private static FeatureDefinition feature(String key, MinecraftVersion minimumVersion,
            ServerCapabilities.Capability[] requiredCapabilities, String[] requiredMaterials,
            String[] requiredEntities, FeatureDefinition.ListenerFactory factory) {
        return new FeatureDefinition(key, minimumVersion, requiredCapabilities, requiredMaterials,
                requiredEntities, factory);
    }

    private static FeatureDefinition.ListenerFactory entityBlockChangeFactory(final String entityType) {
        return (worlds, capabilities) -> new EntityBlockChangeProtectionListener(worlds, entityType);
    }
}

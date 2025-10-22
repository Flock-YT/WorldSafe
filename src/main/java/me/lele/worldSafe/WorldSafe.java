package me.lele.worldSafe;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteCommandsBukkit;
import me.lele.worldSafe.command.WorldSafeCommand;
import me.lele.worldSafe.config.ConfigManager;
import me.lele.worldSafe.listener.blocks.explosioncancel.BedExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosioncancel.RespawnAnchorExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosioncancel.TNTExplosionCancelListener;
import me.lele.worldSafe.listener.blocks.explosionprevention.BedExplosionProtectionListener;
import me.lele.worldSafe.listener.blocks.explosionprevention.RespawnAnchorExplosionPreventionListener;
import me.lele.worldSafe.listener.blocks.explosionprevention.TNTExplosionProtectionListener;
import me.lele.worldSafe.listener.blocks.other.CropTrampleProtectionListener;
import me.lele.worldSafe.listener.blocks.other.DragonEggTeleportationPreventionListener;
import me.lele.worldSafe.listener.entities.explosioncancel.CreeperExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.EndCrystalExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.GhastExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.WitherExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosionprevention.CreeperExplosionProtectionListener;
import me.lele.worldSafe.listener.entities.explosionprevention.EndCrystalExplosionPreventionListener;
import me.lele.worldSafe.listener.entities.explosionprevention.GhastExplosionProtectionListener;
import me.lele.worldSafe.listener.entities.explosionprevention.WitherExplosionProtectionListener;
import me.lele.worldSafe.listener.entities.other.EnderDragonBlockDestructionProtectionListener;
import me.lele.worldSafe.listener.entities.other.EnderManBlockPickupProtectionListener;
import me.lele.worldSafe.listener.entities.other.PhantomDamagePreventionListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

public final class WorldSafe extends JavaPlugin {

    private final List<Listener> listeners = new ArrayList<>();
    private ConfigManager configManager;
    private LiteCommands<CommandSender> liteCommands;
    private Metrics metrics;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        File configFile = new File(getDataFolder(), "config.yml");
        try {
            configManager = new ConfigManager(configFile);
            configureMetrics();
            loadFeatures();
        } catch (ConfigurateException e) {
            getLogger().log(Level.SEVERE, "无法加载配置文件，请检查 config.yml 是否符合要求。", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        } catch (SerializationException e) {
            getLogger().log(Level.SEVERE, "无法加载插件功能，请检查配置格式。", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadCommand();

        getLogger().info("插件加载完毕!");
    }

    @Override
    public void onDisable() {
        unregisterListeners();
        getLogger().info("插件已卸载!");
    }

    private void loadCommand() {
        this.liteCommands = LiteCommandsBukkit.builder("WorldSafe")
                .commands(new WorldSafeCommand(this))
                .build();
    }

    private void loadFeatures() throws SerializationException {
        boolean enabled = configManager.getConfig().node("enabled").getBoolean(true);
        if (!enabled) {
            getLogger().info("插件已禁用，未加载任何功能。");
            return;
        }

        // 方块相关功能
        registerListener("bedExplosionCancel", BedExplosionCancelListener::new);
        registerListener("respawnAnchorExplosionCancel", RespawnAnchorExplosionCancelListener::new);
        registerListener("tntExplosionCancel", TNTExplosionCancelListener::new);

        registerListener("bedExplosionProtection", BedExplosionProtectionListener::new);
        registerListener("respawnAnchorExplosionPrevention", RespawnAnchorExplosionPreventionListener::new);
        registerListener("tntExplosionProtection", TNTExplosionProtectionListener::new);

        registerListener("cropTrampleProtection", CropTrampleProtectionListener::new);
        registerListener("dragonEggTeleportationPrevention", DragonEggTeleportationPreventionListener::new);

        // 实体相关功能
        registerListener("creeperExplosionCancel", CreeperExplosionCancelListener::new);
        registerListener("endCrystalExplosionCancel", EndCrystalExplosionCancelListener::new);
        registerListener("ghastExplosionCancel", GhastExplosionCancelListener::new);
        registerListener("witherExplosionCancel", WitherExplosionCancelListener::new);

        registerListener("creeperExplosionProtection", CreeperExplosionProtectionListener::new);
        registerListener("endCrystalExplosionPrevention", EndCrystalExplosionPreventionListener::new);
        registerListener("ghastExplosionProtection", GhastExplosionProtectionListener::new);
        registerListener("witherExplosionProtection", WitherExplosionProtectionListener::new);

        registerListener("enderDragonBlockDestructionProtection", EnderDragonBlockDestructionProtectionListener::new);
        registerListener("enderManBlockPickupProtection", EnderManBlockPickupProtectionListener::new);
        registerListener("phantomDamagePrevention", PhantomDamagePreventionListener::new);
    }

    public void reloadFeatures() throws ConfigurateException, SerializationException {
        synchronized (listeners) {
            configManager.reloadConfig();
            configureMetrics();
            unregisterListeners();
            loadFeatures();
        }
        getLogger().info("配置已重新加载。");
    }

    private void registerListener(String configNode, Function<List<String>, ? extends Listener> factory)
            throws SerializationException {
        List<String> worlds = configManager.getConfig().node(configNode).getList(String.class, List.of());
        if (worlds.isEmpty()) {
            getLogger().log(Level.FINE, () -> "跳过注册 " + configNode + "，因为未配置任何世界。");
            return;
        }
        Listener listener = factory.apply(worlds);
        getServer().getPluginManager().registerEvents(listener, this);
        listeners.add(listener);
    }

    private void unregisterListeners() {
        listeners.forEach(HandlerList::unregisterAll);
        listeners.clear();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    private void configureMetrics() {
        boolean enableMetrics = configManager.getConfig().node("enabled-bstats").getBoolean(true);
        if (enableMetrics) {
            if (metrics == null) {
                metrics = new Metrics(this, 22831);
            }
        } else if (metrics != null) {
            getLogger().warning("已禁用 bStats，但需要重启服务器才能完全停止数据上报。");
            metrics = null;
        }
    }
}

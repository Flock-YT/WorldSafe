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
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class WorldSafe extends JavaPlugin {

        private final List<Listener> listeners = new ArrayList<>();
	public static ConfigManager configManager;
	private LiteCommands<CommandSender> liteCommands;

	@Override
	public void onEnable() {

		// 确保配置文件存在，如果不存在则创建一个默认的
		saveDefaultConfig();

                // 初始化 ConfigManager
                File configFile = new File(getDataFolder(), "config.yml");
                configManager = new ConfigManager(configFile);

                if (configManager.getConfig() == null) {
                        getLogger().severe("配置加载失败，插件将被禁用。");
                        getServer().getPluginManager().disablePlugin(this);
                        return;
                }

                // 加载功能
                try {
                        loadFeatures();
                } catch (SerializationException e) {
                        String pathInfo = e.path() != null ? e.path().toString() : "未知节点";
                        getLogger().severe("配置节点 " + pathInfo + " 解析失败: " + e.getMessage());
                        getLogger().severe("无法加载插件,请联系开发者QQ:3288732918");
                        getServer().getPluginManager().disablePlugin(this); // 禁用插件
                        return;
                }

		// 加载指令
		loadCommand();

		// 加载bStats
		if (configManager.getConfig().node("enabled-bstats").getBoolean(true)) {
			Metrics metrics = new Metrics(this, 22831);
		}

		// Plugin startup logic

		getLogger().info("插件加载完毕!");

	}

        @Override
        public void onDisable() {
                getLogger().info("插件已卸载!");
                // Plugin shutdown logic
                HandlerList.unregisterAll(this);
                listeners.clear();
        }

	public void loadFeatures() throws SerializationException {
		// 获取配置文件中的总开关
		boolean enabled = configManager.getConfig().node("enabled").getBoolean();
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


	private void loadCommand() {

		// 注册重载命令
		this.liteCommands = LiteCommandsBukkit.builder("WorldSafe").commands(new WorldSafeCommand(this)).build();

	}


        private void registerListener(String configNode, Function<List<String>, ? extends Listener> factory)
                        throws SerializationException {
                List<String> worlds = configManager.getConfig().node(configNode).getList(String.class);
                if (worlds == null || worlds.isEmpty()) {
                        return;
                }
                Listener listener = factory.apply(worlds);
                getServer().getPluginManager().registerEvents(listener, this);
                listeners.add(listener);
        }

        public void resetFeatures() {
                HandlerList.unregisterAll(this);
                listeners.clear();
                try {
                        loadFeatures();
                } catch (SerializationException e) {
                        String pathInfo = e.path() != null ? e.path().toString() : "未知节点";
                        getLogger().severe("重载配置失败，节点 " + pathInfo + " 解析失败: " + e.getMessage());
                }
        }

        public List<Listener> getListeners() {
                return Collections.unmodifiableList(listeners);
        }



}

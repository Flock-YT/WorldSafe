package me.lele.worldSafe;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteCommandsBukkit;
import me.lele.worldSafe.command.WorldSafeCommand;
import me.lele.worldSafe.config.ConfigManager;
import me.lele.worldSafe.listener.CreeperExplosionProtectionListener;
import me.lele.worldSafe.listener.EnderDragonBlockDestructionProtectionListener;
import me.lele.worldSafe.listener.EnderManBlockPickupProtectionListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class WorldSafe extends JavaPlugin {

	public static List<Listener> listeners = new ArrayList<>();
	public static ConfigManager configManager;
	private LiteCommands<CommandSender> liteCommands;

	@Override
	public void onEnable() {

		// 确保配置文件存在，如果不存在则创建一个默认的
		saveDefaultConfig();

		// 初始化 ConfigManager
		File configFile = new File(getDataFolder(), "config.yml");
		configManager = new ConfigManager(configFile);

		// 加载功能
		try {
			loadFeatures();
		} catch (SerializationException e) {
			getLogger().severe("无法加载插件,请联系开发者QQ:3288732918");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this); // 禁用插件
		}

		// 加载指令
		loadCommand();

		// 加载bStats
		Metrics metrics = new Metrics(this, 22831);
		// Plugin startup logic

		getLogger().info("插件加载完毕!");

	}

	@Override
	public void onDisable() {
		getLogger().info("插件已卸载!");
		// Plugin shutdown logic
	}

	public void loadFeatures() throws SerializationException {
		// 获取配置文件中的总开关
		boolean enabled = configManager.getConfig().node("enabled").getBoolean();
		if (!enabled) {
			getLogger().info("插件已禁用，未加载任何功能。");
			return;
		}

		// 获取配置文件中creeperExplosionProtection配置的世界列表
		List<String> creeperExplosionProtection = configManager.getConfig().node("creeperExplosionProtection")
				.getList(String.class);
		if (creeperExplosionProtection != null && !creeperExplosionProtection.isEmpty()) {
			// 注册Creeper监听器
			CreeperExplosionProtectionListener listener = new CreeperExplosionProtectionListener(
					creeperExplosionProtection);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件中enderManBlockPickupProtection配置的世界列表
		List<String> enderManBlockPickupProtection = configManager.getConfig().node("enderManBlockPickupProtection")
				.getList(String.class);
		if (enderManBlockPickupProtection != null && !enderManBlockPickupProtection.isEmpty()) {
			// 注册EnderMan监听器
			EnderManBlockPickupProtectionListener listener = new EnderManBlockPickupProtectionListener(
					enderManBlockPickupProtection);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件中enderManBlockPickupProtection配置的世界列表
		List<String> enderDragonBlockDestructionProtection = configManager.getConfig()
				.node("enderDragonBlockDestructionProtection").getList(String.class);
		if (enderDragonBlockDestructionProtection != null && !enderDragonBlockDestructionProtection.isEmpty()) {
			// 注册Creeper监听器
			EnderDragonBlockDestructionProtectionListener listener = new EnderDragonBlockDestructionProtectionListener(
					enderDragonBlockDestructionProtection);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

	}

	private void loadCommand() {

		// 注册重载命令
		this.liteCommands = LiteCommandsBukkit.builder("WorldSafe").commands(new WorldSafeCommand(this)).build();

	}

	public void reloadFeatures() {
		try {
			loadFeatures();
		} catch (IOException e) {
			getLogger().severe("重载配置失败!");
			e.printStackTrace();
		}
	}

}

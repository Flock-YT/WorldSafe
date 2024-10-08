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
	}

	public void loadFeatures() throws SerializationException {
		// 获取配置文件中的总开关
		boolean enabled = configManager.getConfig().node("enabled").getBoolean();
		if (!enabled) {
			getLogger().info("插件已禁用，未加载任何功能。");
			return;
		}


//TODO:方块类
	// TODO:直接取消爆炸类

		// 获取配置文件bedExplosionCancel配置的世界列表
		List<String> bedExplosionCancel = configManager.getConfig().node("bedExplosionCancel")
				.getList(String.class);
		if (bedExplosionCancel != null && !bedExplosionCancel.isEmpty()) {
			// 注册BedExplosion监听器
			BedExplosionCancelListener listener = new BedExplosionCancelListener(bedExplosionCancel);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件respawnAnchorExplosionCancel配置的世界列表
		List<String> respawnAnchorExplosionCancel = configManager.getConfig().node("respawnAnchorExplosionCancel")
				.getList(String.class);
		if (respawnAnchorExplosionCancel != null && !respawnAnchorExplosionCancel.isEmpty()) {
			// 注册EndCrystal监听器
			RespawnAnchorExplosionCancelListener listener = new RespawnAnchorExplosionCancelListener(respawnAnchorExplosionCancel);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件tntExplosionCancel配置的世界列表
		List<String> tntExplosionCancel = configManager.getConfig().node("tntExplosionCancel")
				.getList(String.class);
		if (tntExplosionCancel != null && !tntExplosionCancel.isEmpty()) {
			// 注册TNT监听器
			TNTExplosionCancelListener listener = new TNTExplosionCancelListener(tntExplosionCancel);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

	// TODO:取消破坏方块保留伤害类

		// 获取配置文件bedExplosionProtection配置的世界列表
		List<String> bedExplosionProtection = configManager.getConfig().node("bedExplosionProtection")
				.getList(String.class);
		if (bedExplosionProtection != null && !bedExplosionProtection.isEmpty()) {
			// 注册BedExplosion监听器
			BedExplosionProtectionListener listener = new BedExplosionProtectionListener(bedExplosionProtection);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件respawnAnchorExplosionPrevention配置的世界列表
		List<String> respawnAnchorExplosionPrevention = configManager.getConfig().node("respawnAnchorExplosionPrevention")
				.getList(String.class);
		if (respawnAnchorExplosionPrevention != null && !respawnAnchorExplosionPrevention.isEmpty()) {
			// 注册EndCrystal监听器
			RespawnAnchorExplosionPreventionListener listener = new RespawnAnchorExplosionPreventionListener(respawnAnchorExplosionPrevention);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件tntExplosionProtection配置的世界列表
		List<String> tntExplosionProtection = configManager.getConfig().node("tntExplosionProtection")
				.getList(String.class);
		if (tntExplosionProtection != null && !tntExplosionProtection.isEmpty()) {
			// 注册TNT监听器
			TNTExplosionProtectionListener listener = new TNTExplosionProtectionListener(tntExplosionProtection);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

	// TODO:其他类

		// 获取配置文件中cropTrampleProtection配置的世界列表
		List<String> cropTrampleProtection = configManager.getConfig().node("cropTrampleProtection")
				.getList(String.class);
		if (cropTrampleProtection != null && !cropTrampleProtection.isEmpty()) {
			// 注册EntityChangeBlock监听器
			CropTrampleProtectionListener listener = new CropTrampleProtectionListener(cropTrampleProtection);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件中dragonEggTeleportationPrevention配置的世界列表
		List<String> dragonEggTeleportationPrevention = configManager.getConfig()
				.node("dragonEggTeleportationPrevention").getList(String.class);
		if (dragonEggTeleportationPrevention != null && !dragonEggTeleportationPrevention.isEmpty()) {
			// 注册DragonEgg监听器
			DragonEggTeleportationPreventionListener listener = new DragonEggTeleportationPreventionListener(
					dragonEggTeleportationPrevention);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

//TODO:实体类
	// TODO:直接取消爆炸类

		// 获取配置文件中creeperExplosionCancel配置的世界列表
		List<String> creeperExplosionCancel = configManager.getConfig().node("creeperExplosionCancel")
				.getList(String.class);
		if (creeperExplosionCancel != null && !creeperExplosionCancel.isEmpty()) {
			// 注册Creeper监听器
			CreeperExplosionCancelListener listener = new CreeperExplosionCancelListener(
					creeperExplosionCancel);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件endCrystalExplosionCancel配置的世界列表
		List<String> endCrystalExplosionCancel = configManager.getConfig().node("endCrystalExplosionCancel")
				.getList(String.class);
		if (endCrystalExplosionCancel != null && !endCrystalExplosionCancel.isEmpty()) {
			// 注册EndCrystal监听器
			EndCrystalExplosionCancelListener listener = new EndCrystalExplosionCancelListener(endCrystalExplosionCancel);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件ghastExplosionCancel配置的世界列表
		List<String> ghastExplosionCancel = configManager.getConfig().node("ghastExplosionCancel")
				.getList(String.class);
		if (ghastExplosionCancel != null && !ghastExplosionCancel.isEmpty()) {
			// 注册TNT监听器
			GhastExplosionCancelListener listener = new GhastExplosionCancelListener(ghastExplosionCancel);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件witherExplosionCancel配置的世界列表
		List<String> witherExplosionCancel = configManager.getConfig().node("witherExplosionCancel")
				.getList(String.class);
		if (witherExplosionCancel != null && !witherExplosionCancel.isEmpty()) {
			// 注册TNT监听器
			WitherExplosionCancelListener listener = new WitherExplosionCancelListener(witherExplosionCancel);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

	// TODO:取消破坏方块保留伤害类

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

		// 获取配置文件endCrystalExplosionPrevention配置的世界列表
		List<String> endCrystalExplosionPrevention = configManager.getConfig().node("endCrystalExplosionPrevention")
				.getList(String.class);
		if (endCrystalExplosionPrevention != null && !endCrystalExplosionPrevention.isEmpty()) {
			// 注册EndCrystal监听器
			EndCrystalExplosionPreventionListener listener = new EndCrystalExplosionPreventionListener(endCrystalExplosionPrevention);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件ghastExplosionProtection配置的世界列表
		List<String> ghastExplosionProtection = configManager.getConfig().node("ghastExplosionProtection")
				.getList(String.class);
		if (ghastExplosionProtection != null && !ghastExplosionProtection.isEmpty()) {
			// 注册TNT监听器
			GhastExplosionProtectionListener listener = new GhastExplosionProtectionListener(ghastExplosionProtection);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

		// 获取配置文件witherExplosionProtection配置的世界列表
		List<String> witherExplosionProtection = configManager.getConfig().node("witherExplosionProtection")
				.getList(String.class);
		if (witherExplosionProtection != null && !witherExplosionProtection.isEmpty()) {
			// 注册Wither监听器
			WitherExplosionProtectionListener listener = new WitherExplosionProtectionListener(witherExplosionProtection);
			getServer().getPluginManager().registerEvents(listener, this);
			// 添加到已注册列表,方便后续取消
			listeners.add(listener);
		}

	// TODO:其他类

		// 获取配置文件中enderDragonBlockDestructionProtection配置的世界列表
		List<String> enderDragonBlockDestructionProtection = configManager.getConfig()
				.node("enderDragonBlockDestructionProtection").getList(String.class);
		if (enderDragonBlockDestructionProtection != null && !enderDragonBlockDestructionProtection.isEmpty()) {
			// 注册EnderDragon监听器
			EnderDragonBlockDestructionProtectionListener listener = new EnderDragonBlockDestructionProtectionListener(
					enderDragonBlockDestructionProtection);
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

		// 获取配置文件中phantomDamagePrevention配置的世界列表
		List<String> phantomDamagePrevention = configManager.getConfig().node("phantomDamagePrevention")
				.getList(String.class);
		if (phantomDamagePrevention != null && !phantomDamagePrevention.isEmpty()) {
			// 注册EnderMan监听器
			PhantomDamagePreventionListener listener = new PhantomDamagePreventionListener(
					phantomDamagePrevention);
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

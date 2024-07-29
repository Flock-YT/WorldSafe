package me.lele.worldSafe;

import me.lele.worldSafe.listener.CreeperExplosionProtectionListener;
import me.lele.worldSafe.listener.EnderManBlockPickupProtectionListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.List;

public final class WorldSafe extends JavaPlugin {

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode rootNode;

    @Override
    public void onEnable() {

        // 加载配置文件
        try {
            loadConfig();
        } catch (ConfigurateException e) {
            getLogger().severe("无法加载配置文件: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this); // 禁用插件
        }

        // 加载功能
        try {
            loadFeatures();
        } catch (SerializationException e) {
            getLogger().severe("无法加载插件,请联系开发者QQ:3288732918: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this); // 禁用插件
        }

        //加载bStats
        Metrics metrics = new Metrics(this, 22831);
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfig() throws ConfigurateException {

            // 初始化配置文件加载器
            Path configPath = getDataFolder().toPath().resolve("config.yml");
            loader = YamlConfigurationLoader.builder().path(configPath).build();

            // 如果配置文件不存在，则保存默认配置文件
            if (!configPath.toFile().exists()) {
                saveResource("config.yml", false);
            }

            rootNode = loader.load(ConfigurationOptions.defaults());

    }

    private void loadFeatures() throws SerializationException {
        // 获取配置文件中的总开关
        boolean enabled = rootNode.node("enabled").getBoolean();
        if (!enabled) {
            getLogger().info("插件已禁用，未加载任何功能。");
            return;
        }

        // 获取配置文件中creeperExplosionProtection配置的世界列表
        List<String> creeperExplosionProtection = rootNode.node("creeperExplosionProtection").getList(String.class);
        if (creeperExplosionProtection != null && !creeperExplosionProtection.isEmpty()) {
            // 注册Creeper监听器
            getServer().getPluginManager().registerEvents(new CreeperExplosionProtectionListener(creeperExplosionProtection), this);
        }

        // 获取配置文件中enderManBlockPickupProtection配置的世界列表
        List<String> enderManBlockPickupProtection = rootNode.node("enderManBlockPickupProtection").getList(String.class);
        if (enderManBlockPickupProtection != null && !enderManBlockPickupProtection.isEmpty()) {
            // 注册Creeper监听器
            getServer().getPluginManager().registerEvents(new EnderManBlockPickupProtectionListener(enderManBlockPickupProtection), this);
        }

    }

}

package me.lele.worldSafe.config;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigManager {

    private final YamlConfigurationLoader loader;
    private ConfigurationNode config;

    public ConfigManager(File configFile) {
        loader = YamlConfigurationLoader.builder()
                .file(configFile)
                .build();
        loadConfig();
    }

    public boolean loadConfig() {
        try {
            config = loader.load();
            // 在此处可以添加您需要初始化或读取的配置值
            return true;
        } catch (ConfigurateException e) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, "无法加载配置文件", e);
            config = null;
            return false;
        }
    }

    public boolean reloadConfig() {
        return loadConfig();
        // 在此处可以添加您需要重新初始化或更新的配置值
    }

    public ConfigurationNode getConfig() {
        return config;
    }

}

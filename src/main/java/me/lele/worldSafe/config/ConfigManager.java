package me.lele.worldSafe.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final YamlConfigurationLoader loader;
    private ConfigurationNode config;

    public ConfigManager(File configFile) {
        loader = YamlConfigurationLoader.builder()
                .file(configFile)
                .build();
        loadConfig();
    }

    public void loadConfig() {
        try {
            config = loader.load();
            // 在此处可以添加您需要初始化或读取的配置值
        } catch (IOException e) {
            e.printStackTrace();
            // 处理加载失败的情况
        }
    }

    public void reloadConfig() {
        loadConfig();
        // 在此处可以添加您需要重新初始化或更新的配置值
    }

    public ConfigurationNode getConfig() {
        return config;
    }

}

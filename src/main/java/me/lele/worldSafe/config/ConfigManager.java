package me.lele.worldSafe.config;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

public class ConfigManager {

    private final YamlConfigurationLoader loader;
    private ConfigurationNode config;

    public ConfigManager(File configFile) throws ConfigurateException {
        loader = YamlConfigurationLoader.builder()
                .file(configFile)
                .build();
        reloadConfig();
    }

    public void reloadConfig() throws ConfigurateException {
        config = loader.load();
    }

    public ConfigurationNode getConfig() {
        return config;
    }

}

package me.lele.worldSafe;

import me.lele.worldSafe.feature.FeatureDefinition;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginMetadataTest {

    @Test
    void pluginDescriptionLoadsOnTheLegacyApiParser() throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream("plugin.yml");
        assertNotNull(input);
        PluginDescriptionFile description = new PluginDescriptionFile(input);
        assertEquals("me.lele.worldSafe.WorldSafe", description.getMain());
        assertTrue(description.getCommands().containsKey("worldsafe"));
    }

    @Test
    void defaultConfigurationContainsEveryRegisteredFeature() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("config.yml");
        assertNotNull(input);
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(input);
        for (FeatureDefinition feature : WorldSafe.FEATURES) {
            assertTrue(configuration.contains(feature.getConfigKey()), feature.getConfigKey());
        }
    }
}

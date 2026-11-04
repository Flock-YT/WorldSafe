package me.lele.worldSafe.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WorldSafeConfig {

    private final boolean enabled;
    private final boolean bStatsEnabled;
    private final Map<String, List<String>> featureWorlds;

    public WorldSafeConfig(boolean enabled, boolean bStatsEnabled, Map<String, List<String>> featureWorlds) {
        this.enabled = enabled;
        this.bStatsEnabled = bStatsEnabled;

        Map<String, List<String>> copy = new LinkedHashMap<String, List<String>>();
        for (Map.Entry<String, List<String>> entry : featureWorlds.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<String>(entry.getValue())));
        }
        this.featureWorlds = Collections.unmodifiableMap(copy);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isBStatsEnabled() {
        return bStatsEnabled;
    }

    public List<String> getWorlds(String featureKey) {
        List<String> worlds = featureWorlds.get(featureKey);
        return worlds == null ? Collections.<String>emptyList() : worlds;
    }
}

package me.lele.worldSafe.listener.entities.other;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;

import java.util.List;

public class SnowGolemSnowTrailPreventionListener extends WorldScopedFeature {

    public SnowGolemSnowTrailPreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSnowForm(EntityBlockFormEvent event) {
        if (!EntityTypeMatcher.matches(event.getEntity(), "SNOWMAN", "SNOW_GOLEM")) {
            return;
        }
        if (MaterialMatcher.matches(event.getNewState(), "SNOW") && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }
}

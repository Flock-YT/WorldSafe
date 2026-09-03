package me.lele.worldSafe.listener.entities.other;

import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class WitherRoseFormationPreventionListener extends WorldScopedFeature {

    public WitherRoseFormationPreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWitherRoseForm(EntityBlockFormEvent event) {
        if (MaterialMatcher.matches(event.getNewState(), "WITHER_ROSE")
                && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWitherRoseChange(EntityChangeBlockEvent event) {
        if (MaterialMatcher.matches(event.getTo(), "WITHER_ROSE")
                && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }
}

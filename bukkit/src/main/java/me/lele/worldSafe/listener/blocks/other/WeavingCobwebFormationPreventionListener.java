package me.lele.worldSafe.listener.blocks.other;

import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class WeavingCobwebFormationPreventionListener extends WorldScopedFeature {

    public WeavingCobwebFormationPreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCobwebForm(EntityBlockFormEvent event) {
        if (MaterialMatcher.matches(event.getNewState(), "WEB", "COBWEB")
                && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCobwebChange(EntityChangeBlockEvent event) {
        if (MaterialMatcher.matches(event.getTo(), "WEB", "COBWEB")
                && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }
}

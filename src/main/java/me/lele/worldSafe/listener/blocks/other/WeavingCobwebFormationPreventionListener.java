package me.lele.worldSafe.listener.blocks.other;

import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class WeavingCobwebFormationPreventionListener extends WorldScopedFeature {

    public WeavingCobwebFormationPreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    public void onCobwebForm(EntityBlockFormEvent event) {
        if (MaterialMatcher.matches(event.getNewState(), "COBWEB") && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCobwebChange(EntityChangeBlockEvent event) {
        if (MaterialMatcher.matches(event.getTo(), "COBWEB") && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }
}

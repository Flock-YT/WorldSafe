package me.lele.worldSafe.listener.entities.blockchange;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class EntityBlockChangeProtectionListener extends WorldScopedFeature {

    private final String[] entityTypeAliases;

    public EntityBlockChangeProtectionListener(List<String> worlds, String... entityTypeAliases) {
        super(worlds);
        this.entityTypeAliases = entityTypeAliases.clone();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!EntityTypeMatcher.matches(event.getEntity(), entityTypeAliases)) {
            return;
        }
        if (isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }
}

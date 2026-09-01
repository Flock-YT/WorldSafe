package me.lele.worldSafe.listener.entities.interaction;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreakDoorEvent;

import java.util.List;

public class MobDoorBreakProtectionListener extends WorldScopedFeature {

    public MobDoorBreakProtectionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    public void onMobBreakDoor(EntityBreakDoorEvent event) {
        if (EntityTypeMatcher.matches(event.getEntity(), "PLAYER")) {
            return;
        }
        if (isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }
}

package me.lele.worldSafe.listener.entities.explosioncancel;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.List;

public class SulfurCubeExplosionCancelListener extends WorldScopedFeature {

    public SulfurCubeExplosionCancelListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSulfurCubeExplosion(EntityExplodeEvent event) {
        if (!isSulfurCube(event.getEntity())) {
            return;
        }
        if (isWorldEnabled(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSulfurCubePrime(ExplosionPrimeEvent event) {
        if (isSulfurCube(event.getEntity()) && isWorldEnabled(getWorld(event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    protected boolean isSulfurCube(Entity entity) {
        return EntityTypeMatcher.matches(entity, "SULFUR_CUBE", "SULPHUR_CUBE");
    }
}

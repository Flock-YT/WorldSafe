package me.lele.worldSafe.listener.entities.other;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.List;

public class BreezeWindChargeImpactCancelListener extends WorldScopedFeature {

    public BreezeWindChargeImpactCancelListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    public void onBreezeWindChargeExplosion(EntityExplodeEvent event) {
        if (isBreezeWindCharge(event.getEntity()) && isWorldEnabled(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreezeWindChargeHit(ProjectileHitEvent event) {
        if (isBreezeWindCharge(event.getEntity()) && isWorldEnabled(getWorld(event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreezeWindChargeInteract(EntityInteractEvent event) {
        if (isBreezeWindCharge(event.getEntity()) && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    protected boolean isBreezeWindCharge(org.bukkit.entity.Entity entity) {
        return EntityTypeMatcher.matches(entity, "BREEZE_WIND_CHARGE");
    }
}

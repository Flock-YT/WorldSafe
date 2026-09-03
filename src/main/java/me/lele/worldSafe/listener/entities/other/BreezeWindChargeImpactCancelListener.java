package me.lele.worldSafe.listener.entities.other;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.compat.ServerCapabilities;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.List;

public class BreezeWindChargeImpactCancelListener extends WorldScopedFeature {

    private final ServerCapabilities capabilities;

    public BreezeWindChargeImpactCancelListener(List<String> worlds) {
        this(worlds, ServerCapabilities.detect());
    }

    public BreezeWindChargeImpactCancelListener(List<String> worlds, ServerCapabilities capabilities) {
        super(worlds);
        this.capabilities = capabilities;
    }

    @EventHandler
    public void onBreezeWindChargeExplosion(EntityExplodeEvent event) {
        if (isBreezeWindCharge(event.getEntity()) && isWorldEnabled(event.getLocation())) {
            capabilities.cancelIfPossible(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreezeWindChargePrime(ExplosionPrimeEvent event) {
        if (isBreezeWindCharge(event.getEntity()) && isWorldEnabled(getWorld(event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreezeWindChargeHit(ProjectileHitEvent event) {
        if (isBreezeWindCharge(event.getEntity()) && isWorldEnabled(getWorld(event.getEntity()))) {
            capabilities.cancelIfPossible(event);
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

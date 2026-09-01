package me.lele.worldSafe.listener.blocks.other;

import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.List;

public class DecoratedPotProjectileProtectionListener extends WorldScopedFeature {

    public DecoratedPotProjectileProtectionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Block hitBlock = event.getHitBlock();
        if (isDecoratedPot(hitBlock) && isWorldEnabled(getWorld(hitBlock))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileChangeBlock(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Projectile)) {
            return;
        }
        if (isDecoratedPot(event.getBlock()) && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    protected boolean isDecoratedPot(Block block) {
        return MaterialMatcher.matches(block, "DECORATED_POT");
    }
}

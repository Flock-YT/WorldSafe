package me.lele.worldSafe.listener.entities.explosioncancel;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.List;

public class CreeperExplosionCancelListener extends WorldScopedFeature {

        public CreeperExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onCreeperExplode(EntityExplodeEvent event) {
                // 检查是否是Creeper的爆炸
                if (isCreeper(event.getEntity())) {
                        if (!isWorldEnabled(event.getLocation())) {
                                return;
                        }
                        // 阻止事件
                        event.setCancelled(true);
                }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onCreeperPrime(ExplosionPrimeEvent event) {
                if (isCreeper(event.getEntity()) && isWorldEnabled(getWorld(event.getEntity()))) {
                        event.setCancelled(true);
                }
        }

        private boolean isCreeper(Entity entity) {
                return EntityTypeMatcher.matches(entity, "CREEPER");
        }
}

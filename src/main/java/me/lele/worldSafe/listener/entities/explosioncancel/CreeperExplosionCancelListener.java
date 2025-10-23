package me.lele.worldSafe.listener.entities.explosioncancel;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class CreeperExplosionCancelListener extends WorldScopedFeature {

        public CreeperExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler
        public void onCreeperExplode(EntityExplodeEvent event) {
                // 检查是否是Creeper的爆炸
                if (event.getEntityType() == EntityType.CREEPER) {
                        if (!isWorldEnabled(event.getLocation())) {
                                return;
                        }
                        // 阻止事件
                        event.setCancelled(true);
                }
        }
}

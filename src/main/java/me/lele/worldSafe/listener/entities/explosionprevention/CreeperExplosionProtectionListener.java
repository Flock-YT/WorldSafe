package me.lele.worldSafe.listener.entities.explosionprevention;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class CreeperExplosionProtectionListener extends WorldScopedFeature {

        public CreeperExplosionProtectionListener(List<String> worlds) {
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
                        event.blockList().clear();
                }
        }
}

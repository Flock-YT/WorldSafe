package me.lele.worldSafe.listener.entities.explosionprevention;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class CreeperExplosionProtectionListener extends AbstractWorldLimitedListener {

        public CreeperExplosionProtectionListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler
        public void onCreeperExplode(EntityExplodeEvent event) {
                // 检查是否是Creeper的爆炸
                if (event.getEntityType() == EntityType.CREEPER) {
                        // 判断是否属于生效的世界
                        if (isWorldEnabled(event.getLocation())) {
                                // 阻止事件
                                event.blockList().clear();
                        }
                }
        }

}

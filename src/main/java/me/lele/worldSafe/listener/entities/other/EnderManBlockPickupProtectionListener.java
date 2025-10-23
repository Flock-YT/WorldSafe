package me.lele.worldSafe.listener.entities.other;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class EnderManBlockPickupProtectionListener extends AbstractWorldLimitedListener {

        public EnderManBlockPickupProtectionListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler
        public void onEnderManBlockPickup(EntityChangeBlockEvent event) {
                // 检查是否是末影人
                if (event.getEntityType() == EntityType.ENDERMAN) {
                        // 判断是否属于生效的世界
                        if (isWorldEnabled(event.getBlock().getWorld())) {
                                // 阻止事件
                                event.setCancelled(true);
                        }

                }

	}

}

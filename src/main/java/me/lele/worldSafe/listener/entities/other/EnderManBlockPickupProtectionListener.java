package me.lele.worldSafe.listener.entities.other;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class EnderManBlockPickupProtectionListener extends WorldScopedFeature {

        public EnderManBlockPickupProtectionListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler
	public void onEnderManBlockPickup(EntityChangeBlockEvent event) {
                // 检查是否是末影人
                if (event.getEntityType() == EntityType.ENDERMAN) {
                        // 获取事件触发的世界
                        if (!isWorldEnabled(getWorld(event.getBlock()))) {
                                return;
                        }
                        // 阻止事件
                        event.setCancelled(true);

                }

        }
}

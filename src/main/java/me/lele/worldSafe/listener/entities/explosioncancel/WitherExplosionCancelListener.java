package me.lele.worldSafe.listener.entities.explosioncancel;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class WitherExplosionCancelListener extends WorldScopedFeature {

        public WitherExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler
	void onExplode(EntityExplodeEvent e) {
		// 检测是否为凋零/凋零头颅
		if (e.getEntityType() != EntityType.WITHER && e.getEntityType() != EntityType.WITHER_SKULL)
			return;
                if (!isWorldEnabled(getWorld(e.getEntity())))
                        return;
                // 取消事件 阻止爆炸
                e.setCancelled(true);
        }
}

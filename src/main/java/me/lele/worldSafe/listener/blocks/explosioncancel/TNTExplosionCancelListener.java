package me.lele.worldSafe.listener.blocks.explosioncancel;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class TNTExplosionCancelListener extends WorldScopedFeature {

        public TNTExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler
	void onTNTExplode(EntityExplodeEvent e) {
                // 检测是否为TNT类实体
                if (e.getEntityType() != EntityType.TNT && e.getEntityType() != EntityType.TNT_MINECART)
                        return;
                if (!isWorldEnabled(getWorld(e.getEntity())))
                        return;
                // 清空爆炸影响的方块
                e.setCancelled(true);
        }
}

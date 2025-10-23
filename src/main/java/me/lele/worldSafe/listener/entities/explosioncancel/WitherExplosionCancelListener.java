package me.lele.worldSafe.listener.entities.explosioncancel;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class WitherExplosionCancelListener extends AbstractWorldLimitedListener {

        public WitherExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler
        void onExplode(EntityExplodeEvent e) {
                // 检测是否为凋零/凋零头颅
                if (e.getEntityType() != EntityType.WITHER && e.getEntityType() != EntityType.WITHER_SKULL)
                        return;
                Entity ent = e.getEntity();
                // 检测是否启用这个世界
                if (!isWorldEnabled(ent))
                        return;
                // 取消事件 阻止爆炸
                e.setCancelled(true);
        }
}

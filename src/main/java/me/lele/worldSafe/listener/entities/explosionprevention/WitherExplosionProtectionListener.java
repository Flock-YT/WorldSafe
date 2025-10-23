package me.lele.worldSafe.listener.entities.explosionprevention;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class WitherExplosionProtectionListener extends AbstractWorldLimitedListener {

        public WitherExplosionProtectionListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler
        void onWitherDestroyBlock(EntityChangeBlockEvent e) {
                // 判断是否为凋零/凋零头
                if (e.getEntityType() != EntityType.WITHER && e.getEntityType() != EntityType.WITHER_SKULL)
                        return;
                Entity ent = e.getEntity();
                // 判断方块和凋零/凋零头是否在同一世界
                if (!e.getBlock().getWorld().equals(ent.getWorld()))
                        return;
                // 判断是否启用该世界
                if (!isWorldEnabled(ent))
                        return;
                // 判断是否破坏方块
                if (e.getTo() != Material.AIR)
                        return;
                // 取消事件
		e.setCancelled(true);
	}
}

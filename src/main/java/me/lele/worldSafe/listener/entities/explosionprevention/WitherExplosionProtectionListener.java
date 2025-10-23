package me.lele.worldSafe.listener.entities.explosionprevention;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class WitherExplosionProtectionListener extends WorldScopedFeature {

        public WitherExplosionProtectionListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler
	void onWitherDestroyBlock(EntityChangeBlockEvent e) {
                // 判断是否为凋零/凋零头
                if (e.getEntityType() != EntityType.WITHER && e.getEntityType() != EntityType.WITHER_SKULL)
                        return;
                World entityWorld = getWorld(e.getEntity());
                if (!isWorldEnabled(entityWorld))
                        return;
                if (!isSameWorld(entityWorld, getWorld(e.getBlock())))
                        return;
                // 判断是否破坏方块
                if (e.getTo() != Material.AIR)
                        return;
                // 取消事件
                e.setCancelled(true);
        }
}

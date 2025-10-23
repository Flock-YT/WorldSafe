package me.lele.worldSafe.listener.entities.other;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class EnderDragonBlockDestructionProtectionListener extends WorldScopedFeature {

        public EnderDragonBlockDestructionProtectionListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler
	void onEnderDragonDestroyBlock(EntityChangeBlockEvent e) {
                // 判断是否为末影龙
                if (e.getEntityType() != EntityType.ENDER_DRAGON)
                        return;
                World dragonWorld = getWorld(e.getEntity());
                if (!isWorldEnabled(dragonWorld))
                        return;
                if (!isSameWorld(dragonWorld, getWorld(e.getBlock())))
                        return;
                // 判断是否破坏方块
                if (e.getTo() != Material.AIR)
                        return;
                // 取消事件
                e.setCancelled(true);
        }
}

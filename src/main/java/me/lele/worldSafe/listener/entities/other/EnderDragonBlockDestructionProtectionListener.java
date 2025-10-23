package me.lele.worldSafe.listener.entities.other;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class EnderDragonBlockDestructionProtectionListener extends AbstractWorldLimitedListener {

        public EnderDragonBlockDestructionProtectionListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler
        void onEnderDragonDestroyBlock(EntityChangeBlockEvent e) {
                // 判断是否为末影龙
                if (e.getEntityType() != EntityType.ENDER_DRAGON)
                        return;
                Entity ed = e.getEntity();
                // 判断方块和末影龙是否在同一世界
                if (!e.getBlock().getWorld().equals(ed.getWorld()))
                        return;
                // 判断是否启用该世界
                if (!isWorldEnabled(ed))
                        return;
                // 判断是否破坏方块
                if (e.getTo() != Material.AIR)
                        return;
                // 取消事件
		e.setCancelled(true);
	}
}

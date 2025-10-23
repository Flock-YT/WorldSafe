package me.lele.worldSafe.listener.blocks.other;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class CropTrampleProtectionListener extends WorldScopedFeature {

        public CropTrampleProtectionListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler
	void onBlockChangeByEntity(EntityChangeBlockEvent e) {
		Block b = e.getBlock();
		// 判断是否为耕地
		if (b.getType() != Material.FARMLAND)
			return;
                // 检测是否为踩踏
                if (e.getTo() != Material.DIRT && e.getTo() != Material.GRASS_BLOCK)
                        return;
                if (!isWorldEnabled(getWorld(b)))
                        return;
                // 取消事件
                e.setCancelled(true);
        }
}

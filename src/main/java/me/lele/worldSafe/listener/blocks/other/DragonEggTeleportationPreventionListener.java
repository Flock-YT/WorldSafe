package me.lele.worldSafe.listener.blocks.other;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class DragonEggTeleportationPreventionListener extends AbstractWorldLimitedListener {

        public DragonEggTeleportationPreventionListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler
        void onDragonEggTeleport(BlockFromToEvent e) {
                Block b = e.getBlock();
                // 检测方块是否为龙蛋
                if (b.getType() != Material.DRAGON_EGG)
                        return;
                // 判断是否启动这个世界
                if (!isWorldEnabled(b.getWorld()))
                        return;
                // 取消事件 即禁止龙蛋瞬移
                e.setCancelled(true);
        }
}

package me.lele.worldSafe.listener.blocks.explosioncancel;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

public class RespawnAnchorExplosionCancel implements Listener {

    private final List<String> worlds;

    public RespawnAnchorExplosionCancel(List<String> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    void onRespawnAnchorExplosion(BlockExplodeEvent e) {

        // 检测是否为 重生锚 爆炸
        if (e.getExplodedBlockState().getBlockData().getMaterial() == Material.RESPAWN_ANCHOR) {
            // 判断是否启用这个世界
            if (!worlds.contains(e.getExplodedBlockState().getLocation().getWorld().getName()))
                return;
            // 清空爆炸影响的方块
            e.setCancelled(true);

        }

    }

}
package me.lele.worldSafe.listener.blocks.explosionprevention;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class RespawnAnchorExplosionPreventionListener extends AbstractWorldLimitedListener {

    public RespawnAnchorExplosionPreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    void onRespawnAnchorExplosion(BlockExplodeEvent e) {

        // 检测是否为 重生锚 爆炸
        if (e.getExplodedBlockState().getBlockData().getMaterial() == Material.RESPAWN_ANCHOR) {
            // 判断是否启用这个世界
            if (!isWorldEnabled(e.getExplodedBlockState().getLocation()))
                return;
            // 清空爆炸影响的方块
            e.blockList().clear();

        }

    }

}

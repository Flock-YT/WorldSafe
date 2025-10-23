package me.lele.worldSafe.listener.blocks.explosionprevention;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

public class RespawnAnchorExplosionPreventionListener extends WorldScopedFeature {

    public RespawnAnchorExplosionPreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    void onRespawnAnchorExplosion(BlockExplodeEvent e) {

        BlockState explodedBlockState = e.getExplodedBlockState();
        if (explodedBlockState == null) {
            return;
        }

        // 检测是否为 重生锚 爆炸
        if (explodedBlockState.getBlockData().getMaterial() == Material.RESPAWN_ANCHOR) {
            if (!isWorldEnabled(explodedBlockState.getLocation())) {
                return;
            }
            // 清空爆炸影响的方块
            e.blockList().clear();

        }

    }
}

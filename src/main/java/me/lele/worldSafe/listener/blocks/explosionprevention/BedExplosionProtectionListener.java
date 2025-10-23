package me.lele.worldSafe.listener.blocks.explosionprevention;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

public class BedExplosionProtectionListener extends WorldScopedFeature {

    public BedExplosionProtectionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    void onBedExplosion(BlockExplodeEvent e) {

        BlockState explodedBlockState = e.getExplodedBlockState();
        if (explodedBlockState == null) {
            return;
        }

        // 检测是否为 床 爆炸
        if (explodedBlockState.getBlockData() instanceof Bed){
            if (!isWorldEnabled(explodedBlockState.getLocation())) {
                return;
            }
            // 清空爆炸影响的方块
            e.blockList().clear();

        }

    }
}

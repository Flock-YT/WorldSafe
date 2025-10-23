package me.lele.worldSafe.listener.blocks.explosionprevention;

import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.Collection;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class BedExplosionProtectionListener extends AbstractWorldLimitedListener {

    public BedExplosionProtectionListener(Collection<String> worlds) {
        super(worlds);
    }

    @EventHandler
    void onBedExplosion(BlockExplodeEvent e) {

        // 检测是否为 床 爆炸
        if (e.getExplodedBlockState().getBlockData() instanceof Bed){
            // 判断是否启用这个世界
            if (!isWorldEnabled(e.getExplodedBlockState().getLocation()))
                return;
            // 清空爆炸影响的方块
            e.blockList().clear();

        }

    }

}

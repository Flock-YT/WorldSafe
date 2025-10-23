package me.lele.worldSafe.listener.blocks.explosionprevention;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

public class BedExplosionProtectionListener implements Listener {

    private final List<String> worlds;

    public BedExplosionProtectionListener(List<String> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    void onBedExplosion(BlockExplodeEvent e) {

        BlockState explodedBlockState = e.getExplodedBlockState();
        if (explodedBlockState == null) {
            return;
        }

        // 检测是否为 床 爆炸
        if (explodedBlockState.getBlockData() instanceof Bed){
            World world = getWorld(explodedBlockState.getLocation());
            if (!isWorldEnabled(world)) {
                return;
            }
            // 清空爆炸影响的方块
            e.blockList().clear();

        }

    }

    private World getWorld(Location location) {
        return location != null ? location.getWorld() : null;
    }

    private boolean isWorldEnabled(World world) {
        return world != null && worlds.contains(world.getName());
    }

}

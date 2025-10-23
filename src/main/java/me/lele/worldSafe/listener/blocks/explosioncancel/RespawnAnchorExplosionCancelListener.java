package me.lele.worldSafe.listener.blocks.explosioncancel;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

public class RespawnAnchorExplosionCancelListener implements Listener {

    private final List<String> worlds;

    public RespawnAnchorExplosionCancelListener(List<String> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    void onRespawnAnchorExplosion(BlockExplodeEvent e) {

        BlockState explodedBlockState = e.getExplodedBlockState();
        if (explodedBlockState == null) {
            return;
        }

        // 检测是否为 重生锚 爆炸
        if (explodedBlockState.getBlockData().getMaterial() == Material.RESPAWN_ANCHOR) {
            World world = getWorld(explodedBlockState.getLocation());
            if (!isWorldEnabled(world)) {
                return;
            }
            // 清空爆炸影响的方块
            e.setCancelled(true);

        }

    }

    private World getWorld(Location location) {
        return location != null ? location.getWorld() : null;
    }

    private boolean isWorldEnabled(World world) {
        return world != null && worlds.contains(world.getName());
    }

}

package me.lele.worldSafe.listener.entities.explosionprevention;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class EndCrystalExplosionPreventionListener implements Listener {

    private final List<String> worlds;

    public EndCrystalExplosionPreventionListener(List<String> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // 检查爆炸实体是否是末地水晶
        if (isEndCrystal(event.getEntity())) {
            World world = getWorld(event.getLocation());
            if (!isWorldEnabled(world)) {
                return;
            }
            // 清空爆炸影响的方块
            event.blockList().clear();
        }
    }

    private boolean isEndCrystal(Entity entity) {
        return entity != null && entity.getType() == EntityType.END_CRYSTAL;
    }

    private World getWorld(Location location) {
        return location != null ? location.getWorld() : null;
    }

    private boolean isWorldEnabled(World world) {
        return world != null && worlds.contains(world.getName());
    }

}

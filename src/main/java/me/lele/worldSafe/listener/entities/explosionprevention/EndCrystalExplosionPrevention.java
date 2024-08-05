package me.lele.worldSafe.listener.entities.explosionprevention;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class EndCrystalExplosionPrevention implements Listener {

    private final List<String> worlds;

    public EndCrystalExplosionPrevention(List<String> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // 检查爆炸实体是否是末地水晶
        if (event.getEntity().getType() == EntityType.END_CRYSTAL) {
            // 判断是否启用这个世界
            if (!worlds.contains(event.getLocation().getWorld().getName()))
                return;
            // 清空爆炸影响的方块
            event.blockList().clear();
        }
    }

}

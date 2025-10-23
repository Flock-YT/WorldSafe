package me.lele.worldSafe.listener.entities.explosioncancel;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class EndCrystalExplosionCancelListener extends WorldScopedFeature {

    public EndCrystalExplosionCancelListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // 检查爆炸实体是否是末地水晶
        if (isEndCrystal(event.getEntity())) {
            if (!isWorldEnabled(event.getLocation())) {
                return;
            }
            // 清空爆炸影响的方块
            event.setCancelled(true);
        }
    }

    private boolean isEndCrystal(Entity entity) {
        return entity != null && entity.getType() == EntityType.END_CRYSTAL;
    }

}

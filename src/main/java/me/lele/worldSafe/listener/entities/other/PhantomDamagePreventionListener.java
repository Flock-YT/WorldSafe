package me.lele.worldSafe.listener.entities.other;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

public class PhantomDamagePreventionListener extends WorldScopedFeature {

    public PhantomDamagePreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // 检测造成伤害的实体是否为幻翼
        Entity damager = e.getDamager();
        if (!isPhantom(damager))
            return;
        // 检测此世界是否启用
        if (!isWorldEnabled(getWorld(damager)))
            return;
        //消除幻翼伤害
        e.setCancelled(true);
    }

    private boolean isPhantom(Entity entity) {
        return entity != null && entity.getType() == EntityType.PHANTOM;
    }

}

package me.lele.worldSafe.listener.entities.other;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class PhantomDamagePreventionListener extends AbstractWorldLimitedListener {

    public PhantomDamagePreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // 检测造成伤害的实体是否为幻翼
        if (e.getDamager().getType() != EntityType.PHANTOM)
            return;
        // 检测此世界是否启用
        if (!isWorldEnabled(e.getDamager()))
            return;
        //消除幻翼伤害
        e.setCancelled(true);
    }

}

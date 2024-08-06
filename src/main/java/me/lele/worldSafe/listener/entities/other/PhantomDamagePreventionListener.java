package me.lele.worldSafe.listener.entities.other;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

public class PhantomDamagePreventionListener implements Listener {

    // 定义生效的世界
    private final List<String> worlds;

    // 把配置文件中的生效世界赋值到worlds
    public PhantomDamagePreventionListener(List<String> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // 检测造成伤害的实体是否为幻翼
        if (e.getDamager().getType() != EntityType.PHANTOM)
            return;
        // 检测此世界是否启用
        if (!worlds.contains(e.getDamager().getWorld().getName()))
            return;
        //消除幻翼伤害
        e.setCancelled(true);
    }

}

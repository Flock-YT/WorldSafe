package me.lele.worldSafe.listener.entities.other;

import org.bukkit.World;
import org.bukkit.entity.Entity;
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
        Entity damager = e.getDamager();
        if (!isPhantom(damager))
            return;
        // 检测此世界是否启用
        World world = getWorld(damager);
        if (!isWorldEnabled(world))
            return;
        //消除幻翼伤害
        e.setCancelled(true);
    }

    private boolean isPhantom(Entity entity) {
        return entity != null && entity.getType() == EntityType.PHANTOM;
    }

    private World getWorld(Entity entity) {
        return entity != null ? entity.getWorld() : null;
    }

    private boolean isWorldEnabled(World world) {
        return world != null && worlds.contains(world.getName());
    }

}

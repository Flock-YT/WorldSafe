package me.lele.worldSafe.listener.entities.other;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class EnderDragonBlockDestructionProtectionListener extends WorldScopedFeature {

        public EnderDragonBlockDestructionProtectionListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler
	public void onEnderDragonDestroyBlock(EntityExplodeEvent e) {
                if (e.getEntityType() != EntityType.ENDER_DRAGON)
                        return;
                if (!isWorldEnabled(e.getLocation()))
                        return;
                e.blockList().clear();
        }
}

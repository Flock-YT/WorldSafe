package me.lele.worldSafe.listener;

import me.lele.worldSafe.core.WorldSelector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Set;

public abstract class WorldScopedFeature implements Listener {

        private final WorldSelector worlds;

        protected WorldScopedFeature(Collection<String> worlds) {
                this.worlds = new WorldSelector(worlds, WorldSelector::normalize);
        }

        protected World getWorld(Location location) {
                return location != null ? location.getWorld() : null;
        }

        protected World getWorld(Entity entity) {
                return entity != null ? entity.getWorld() : null;
        }

        protected World getWorld(Block block) {
                return block != null ? block.getWorld() : null;
        }

        protected boolean isWorldEnabled(Location location) {
                return isWorldEnabled(getWorld(location));
        }

        protected boolean isWorldEnabled(World world) {
                return world != null && worlds.matches(world.getName());
        }

        protected boolean isWorldEnabled(String worldName) {
                return worldName != null && worlds.matches(worldName);
        }

        protected boolean isSameWorld(World first, World second) {
                return first != null && first.equals(second);
        }

        protected Set<String> getConfiguredWorlds() {
                return worlds.names();
        }

}

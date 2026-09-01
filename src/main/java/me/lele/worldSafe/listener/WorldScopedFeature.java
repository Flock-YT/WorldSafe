package me.lele.worldSafe.listener;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public abstract class WorldScopedFeature implements Listener {

        private final Set<String> worlds;

        protected WorldScopedFeature(Collection<String> worlds) {
                if (worlds == null || worlds.isEmpty()) {
                        this.worlds = Collections.emptySet();
                        return;
                }
                Set<String> normalized = new LinkedHashSet<String>();
                for (String world : worlds) {
                        if (world != null && !world.trim().isEmpty()) {
                                normalized.add(world.trim().toLowerCase(Locale.ROOT));
                        }
                }
                this.worlds = Collections.unmodifiableSet(normalized);
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
                return world != null && worlds.contains(normalize(world.getName()));
        }

        protected boolean isWorldEnabled(String worldName) {
                return worldName != null && worlds.contains(normalize(worldName));
        }

        protected boolean isSameWorld(World first, World second) {
                return first != null && first.equals(second);
        }

        protected Set<String> getConfiguredWorlds() {
                return worlds;
        }

        private String normalize(String worldName) {
                return worldName.trim().toLowerCase(Locale.ROOT);
        }
}

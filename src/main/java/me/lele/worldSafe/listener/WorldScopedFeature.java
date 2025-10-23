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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class WorldScopedFeature implements Listener {

        private final Set<String> worlds;

        protected WorldScopedFeature(Collection<String> worlds) {
                if (worlds == null || worlds.isEmpty()) {
                        this.worlds = Set.of();
                        return;
                }
                this.worlds = Collections.unmodifiableSet(worlds.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .map(name -> name.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
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

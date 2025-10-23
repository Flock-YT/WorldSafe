package me.lele.worldSafe.listener;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Shared base class for listeners that only apply to specific worlds. It centralises null handling
 * and prevents code duplication when checking whether a world is enabled.
 */
public abstract class AbstractWorldLimitedListener implements Listener {

    private final Set<String> worlds;

    protected AbstractWorldLimitedListener(Collection<String> worlds) {
        if (worlds == null || worlds.isEmpty()) {
            this.worlds = Collections.emptySet();
        } else {
            this.worlds = Collections.unmodifiableSet(new HashSet<>(worlds));
        }
    }

    protected final boolean isWorldEnabled(String worldName) {
        if (worldName == null) {
            return false;
        }
        return worlds.contains(worldName);
    }

    protected final boolean isWorldEnabled(World world) {
        if (world == null) {
            return false;
        }
        return isWorldEnabled(world.getName());
    }

    protected final boolean isWorldEnabled(Location location) {
        if (location == null) {
            return false;
        }
        return isWorldEnabled(location.getWorld());
    }

    protected final boolean isWorldEnabled(Entity entity) {
        if (entity == null) {
            return false;
        }
        return isWorldEnabled(entity.getWorld());
    }
}

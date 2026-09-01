package me.lele.worldSafe.compat;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockExplodeEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;

public final class BlockExplosionSourceResolver {

    private static final long DEFAULT_TTL_NANOS = Duration.ofSeconds(2).toNanos();

    private final Map<BlockKey, TrackedSource> recentSources = new HashMap<>();
    private final LongSupplier nanoTime;
    private final long ttlNanos;

    public BlockExplosionSourceResolver() {
        this(System::nanoTime, DEFAULT_TTL_NANOS);
    }

    BlockExplosionSourceResolver(LongSupplier nanoTime, long ttlNanos) {
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
        this.ttlNanos = ttlNanos;
    }

    public void remember(Block block) {
        if (block == null) {
            return;
        }
        purgeExpired();
        recentSources.put(BlockKey.from(block), new TrackedSource(block.getType().name(), nanoTime.getAsLong()));
    }

    public boolean isSource(BlockExplodeEvent event, String... materialAliases) {
        if (event == null) {
            return false;
        }

        BlockState explodedState = getExplodedBlockState(event);
        if (explodedState != null && MaterialMatcher.matches(explodedState, materialAliases)) {
            return true;
        }

        Block eventBlock = event.getBlock();
        if (eventBlock != null && eventBlock.getType() != Material.AIR
                && MaterialMatcher.matches(eventBlock, materialAliases)) {
            return true;
        }

        purgeExpired();
        TrackedSource tracked = eventBlock == null ? null : recentSources.remove(BlockKey.from(eventBlock));
        return tracked != null && MaterialMatcher.matchesName(tracked.materialName(), materialAliases);
    }

    private BlockState getExplodedBlockState(BlockExplodeEvent event) {
        try {
            Method method = event.getClass().getMethod("getExplodedBlockState");
            Object state = method.invoke(event);
            return state instanceof BlockState ? (BlockState) state : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private void purgeExpired() {
        long now = nanoTime.getAsLong();
        Iterator<TrackedSource> iterator = recentSources.values().iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().createdAtNanos() > ttlNanos) {
                iterator.remove();
            }
        }
    }

    private record TrackedSource(String materialName, long createdAtNanos) {
    }

    private record BlockKey(String worldName, int x, int y, int z) {
        private static BlockKey from(Block block) {
            World world = block.getWorld();
            String worldName = world == null ? "" : world.getName();
            return new BlockKey(worldName, block.getX(), block.getY(), block.getZ());
        }
    }
}

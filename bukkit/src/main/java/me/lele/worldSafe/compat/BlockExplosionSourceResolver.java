package me.lele.worldSafe.compat;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;

public final class BlockExplosionSourceResolver {

    private static final long DEFAULT_TTL_NANOS = 2_000_000_000L;

    private final Map<BlockKey, TrackedSource> recentSources = new HashMap<>();
    private final LongSupplier nanoTime;
    private final long ttlNanos;
    private final ServerCapabilities capabilities;

    public BlockExplosionSourceResolver() {
        this(ServerCapabilities.detect());
    }

    public BlockExplosionSourceResolver(ServerCapabilities capabilities) {
        this(capabilities, System::nanoTime, DEFAULT_TTL_NANOS);
    }

    BlockExplosionSourceResolver(LongSupplier nanoTime, long ttlNanos) {
        this(ServerCapabilities.detect(), nanoTime, ttlNanos);
    }

    BlockExplosionSourceResolver(ServerCapabilities capabilities, LongSupplier nanoTime, long ttlNanos) {
        this.capabilities = Objects.requireNonNull(capabilities, "capabilities");
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
        this.ttlNanos = ttlNanos;
    }

    public void remember(Block block) {
        if (block == null) {
            return;
        }
        purgeExpired();
        Block otherHalf = BedStructureResolver.DETECTED.getOtherHalf(block);
        BlockKey primaryKey = BlockKey.from(block);
        BlockKey otherKey = otherHalf == null ? null : BlockKey.from(otherHalf);
        consume(primaryKey);
        if (otherKey != null) {
            consume(otherKey);
        }
        TrackedSource source = new TrackedSource(block.getType().name(), nanoTime.getAsLong(), primaryKey, otherKey);
        recentSources.put(primaryKey, source);
        if (otherKey != null) {
            recentSources.put(otherKey, source);
        }
    }

    public boolean isSource(BlockExplodeEvent event, String... materialAliases) {
        if (event == null) {
            return false;
        }

        purgeExpired();
        Block eventBlock = event.getBlock();
        TrackedSource tracked = eventBlock == null ? null : consume(BlockKey.from(eventBlock));

        BlockState explodedState = capabilities.getExplodedBlockState(event);
        if (explodedState != null && explodedState.getType() != null
                && !MaterialMatcher.matches(explodedState, "AIR", "CAVE_AIR", "VOID_AIR")) {
            return MaterialMatcher.matches(explodedState, materialAliases);
        }

        if (eventBlock != null && eventBlock.getType() != Material.AIR
                && MaterialMatcher.matches(eventBlock, materialAliases)) {
            return true;
        }

        return tracked != null && MaterialMatcher.matchesName(tracked.getMaterialName(), materialAliases);
    }

    private TrackedSource consume(BlockKey key) {
        TrackedSource source = recentSources.remove(key);
        if (source != null) {
            recentSources.remove(source.primaryKey, source);
            if (source.otherKey != null) {
                recentSources.remove(source.otherKey, source);
            }
        }
        return source;
    }

    private void purgeExpired() {
        long now = nanoTime.getAsLong();
        Iterator<TrackedSource> iterator = recentSources.values().iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().getCreatedAtNanos() > ttlNanos) {
                iterator.remove();
            }
        }
    }

    private static final class TrackedSource {
        private final String materialName;
        private final long createdAtNanos;
        private final BlockKey primaryKey;
        private final BlockKey otherKey;

        private TrackedSource(String materialName, long createdAtNanos, BlockKey primaryKey, BlockKey otherKey) {
            this.materialName = materialName;
            this.createdAtNanos = createdAtNanos;
            this.primaryKey = primaryKey;
            this.otherKey = otherKey;
        }

        private String getMaterialName() {
            return materialName;
        }

        private long getCreatedAtNanos() {
            return createdAtNanos;
        }
    }

    private static final class BlockKey {
        private final String worldName;
        private final int x;
        private final int y;
        private final int z;

        private BlockKey(String worldName, int x, int y, int z) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static BlockKey from(Block block) {
            World world = block.getWorld();
            String worldName = world == null ? "" : world.getName();
            return new BlockKey(worldName, block.getX(), block.getY(), block.getZ());
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof BlockKey)) {
                return false;
            }
            BlockKey key = (BlockKey) other;
            return x == key.x && y == key.y && z == key.z && worldName.equals(key.worldName);
        }

        @Override
        public int hashCode() {
            int result = worldName.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }
}

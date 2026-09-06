package me.lele.worldSafe.compat;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Bed;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Method;

final class BedStructureResolver {
    static final BedStructureResolver DETECTED = detect();

    private final Method blockDataMethod;
    private final Method facingMethod;
    private final Method partMethod;

    BedStructureResolver(Method blockDataMethod, Method facingMethod, Method partMethod) {
        this.blockDataMethod = blockDataMethod;
        this.facingMethod = facingMethod;
        this.partMethod = partMethod;
    }

    private static BedStructureResolver detect() {
        Method blockData = null;
        try {
            blockData = Block.class.getMethod("getBlockData");
            ClassLoader loader = BedStructureResolver.class.getClassLoader();
            Class<?> bed = Class.forName("org.bukkit.block.data.type.Bed", false, loader);
            return new BedStructureResolver(blockData, bed.getMethod("getFacing"), bed.getMethod("getPart"));
        } catch (ReflectiveOperationException | LinkageError | SecurityException ignored) {
            return new BedStructureResolver(blockData, null, null);
        }
    }

    Block getOtherHalf(Block block) {
        if (!MaterialMatcher.isBed(block)) {
            return null;
        }
        try {
            Half half = read(block);
            if (half == null) {
                return null;
            }
            BlockFace offset = half.head ? half.facing.getOppositeFace() : half.facing;
            Block other = block.getRelative(offset);
            if (!MaterialMatcher.isBed(other) || other.getType() != block.getType()) {
                return null;
            }
            Half counterpart = read(other);
            return counterpart != null && counterpart.head != half.head && counterpart.facing == half.facing
                    ? other : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            // An unknown structure must not turn unrelated neighboring blocks into explosion sources.
            return null;
        }
    }

    private Half read(Block block) throws ReflectiveOperationException {
        BlockFace facing;
        boolean head;
        if (blockDataMethod != null) {
            if (facingMethod == null || partMethod == null) {
                return null;
            }
            Object data = blockDataMethod.invoke(block);
            if (data == null) {
                return null;
            }
            Object direction = facingMethod.invoke(data);
            Object part = partMethod.invoke(data);
            if (!(direction instanceof BlockFace) || !(part instanceof Enum<?>)) {
                return null;
            }
            String name = ((Enum<?>) part).name();
            if (!"HEAD".equals(name) && !"FOOT".equals(name)) {
                return null;
            }
            facing = (BlockFace) direction;
            head = "HEAD".equals(name);
        } else {
            BlockState state = block.getState();
            MaterialData data = state == null ? null : state.getData();
            if (!(data instanceof Bed)) {
                return null;
            }
            facing = ((Bed) data).getFacing();
            head = ((Bed) data).isHeadOfBed();
        }
        if (facing != BlockFace.NORTH && facing != BlockFace.SOUTH
                && facing != BlockFace.EAST && facing != BlockFace.WEST) {
            return null;
        }
        return new Half(facing, head);
    }

    private static final class Half {
        private final BlockFace facing;
        private final boolean head;

        private Half(BlockFace facing, boolean head) {
            this.facing = facing;
            this.head = head;
        }
    }
}

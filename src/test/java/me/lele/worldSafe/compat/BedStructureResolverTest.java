package me.lele.worldSafe.compat;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Bed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BedStructureResolverTest {
    @Test
    void resolvesBothHalvesInAllDirectionsOnLegacyAndModernApis() throws Exception {
        for (boolean modern : new boolean[] {false, true}) {
            BedStructureResolver resolver = resolver(modern);
            for (BlockFace facing : new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
                Block foot = half(modern, facing, false);
                Block head = half(modern, facing, true);
                when(foot.getRelative(facing)).thenReturn(head);
                when(head.getRelative(facing.getOppositeFace())).thenReturn(foot);
                assertSame(head, resolver.getOtherHalf(foot));
                assertSame(foot, resolver.getOtherHalf(head));
            }
        }
    }

    @Test
    void rejectsMissingMismatchedOrUnrelatedNeighbors() throws Exception {
        for (boolean modern : new boolean[] {false, true}) {
            BedStructureResolver resolver = resolver(modern);
            Block foot = half(modern, BlockFace.NORTH, false);
            assertNull(resolver.getOtherHalf(foot));
            Block samePart = half(modern, BlockFace.NORTH, false);
            when(foot.getRelative(BlockFace.NORTH)).thenReturn(samePart);
            assertNull(resolver.getOtherHalf(foot));
            Block wrongFacing = half(modern, BlockFace.SOUTH, true);
            when(foot.getRelative(BlockFace.NORTH)).thenReturn(wrongFacing);
            assertNull(resolver.getOtherHalf(foot));
            Block wrongMaterial = half(modern, BlockFace.NORTH, true);
            when(wrongMaterial.getType()).thenReturn(Material.STONE);
            when(foot.getRelative(BlockFace.NORTH)).thenReturn(wrongMaterial);
            assertNull(resolver.getOtherHalf(foot));
            Material otherColor = mock(Material.class);
            when(otherColor.name()).thenReturn("BLUE_BED");
            when(wrongMaterial.getType()).thenReturn(otherColor);
            assertNull(resolver.getOtherHalf(foot));
            when(foot.getRelative(BlockFace.NORTH)).thenThrow(new IllegalStateException("unavailable"));
            assertNull(resolver.getOtherHalf(foot));
            verify(foot, never()).getRelative(BlockFace.EAST);
            verify(foot, never()).getRelative(BlockFace.SOUTH);
            verify(foot, never()).getRelative(BlockFace.WEST);
        }
    }

    @Test
    void unknownModernDataAndReflectionFailuresDoNotGuessLegacyGeometry() throws Exception {
        BedStructureResolver resolver = resolver(true);
        Block block = half(true, BlockFace.NORTH, false);
        for (Object data : new Object[] {null, new Object(), new ModernData(BlockFace.UP, Part.FOOT),
                new ModernData(BlockFace.NORTH, Part.UNKNOWN)}) {
            when(((ModernBlock) block).getBlockData()).thenReturn(data);
            assertNull(resolver.getOtherHalf(block));
        }
        when(((ModernBlock) block).getBlockData()).thenThrow(new NoSuchMethodError("missing runtime API"));
        assertNull(resolver.getOtherHalf(block));
        verify(block, never()).getState();
        BedStructureResolver partial = new BedStructureResolver(ModernBlock.class.getMethod("getBlockData"), null, null);
        assertNull(partial.getOtherHalf(block));
    }

    @Test
    void missingOrFailingLegacyStateIsSafe() {
        BedStructureResolver resolver = new BedStructureResolver(null, null, null);
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(Material.BED_BLOCK);
        assertNull(resolver.getOtherHalf(block));
        when(block.getState()).thenThrow(new IllegalStateException("unavailable"));
        assertNull(resolver.getOtherHalf(block));
        assertNull(resolver.getOtherHalf(null));
    }

    private BedStructureResolver resolver(boolean modern) throws Exception {
        return modern ? new BedStructureResolver(ModernBlock.class.getMethod("getBlockData"),
                ModernData.class.getMethod("getFacing"), ModernData.class.getMethod("getPart"))
                : new BedStructureResolver(null, null, null);
    }

    private Block half(boolean modern, BlockFace facing, boolean head) {
        Block block = mock(Block.class, withSettings().extraInterfaces(ModernBlock.class));
        when(block.getType()).thenReturn(Material.BED_BLOCK);
        if (modern) {
            when(((ModernBlock) block).getBlockData()).thenReturn(new ModernData(facing, head ? Part.HEAD : Part.FOOT));
        } else {
            Bed data = new Bed(facing);
            data.setHeadOfBed(head);
            BlockState state = mock(BlockState.class);
            when(state.getData()).thenReturn(data);
            when(block.getState()).thenReturn(state);
        }
        return block;
    }

    public interface ModernBlock {
        Object getBlockData();
    }

    public enum Part { HEAD, FOOT, UNKNOWN }

    public static final class ModernData {
        private final BlockFace facing;
        private final Part part;

        ModernData(BlockFace facing, Part part) {
            this.facing = facing;
            this.part = part;
        }

        public BlockFace getFacing() { return facing; }
        public Part getPart() { return part; }
    }
}

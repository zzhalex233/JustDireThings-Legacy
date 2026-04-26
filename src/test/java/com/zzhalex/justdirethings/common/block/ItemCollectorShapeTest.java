package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.common.block.machine.BlockItemCollector;
import com.zzhalex.justdirethings.common.block.machine.BlockMachineBase;
import com.zzhalex.justdirethings.registry.ModBlocks;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemCollectorShapeTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void itemCollectorDoesNotRenderAsFullOpaqueCube() {
        BlockItemCollector itemCollector = ModBlocks.ITEMCOLLECTOR;
        IBlockState state = itemCollector.getDefaultState();

        assertFalse(itemCollector.isFullCube(state), "Item Collector should not render as a full cube shell");
        assertFalse(itemCollector.isOpaqueCube(state), "Item Collector should not render as an opaque cube shell");
        assertFalse(itemCollector.isFullBlock(state), "Item Collector should not advertise full-block behavior");
        assertFalse(itemCollector.causesSuffocation(state), "Item Collector should not suffocate like a full cube");
    }

    @Test
    void itemCollectorUsesUpstreamDirectionalBounds() {
        BlockItemCollector itemCollector = ModBlocks.ITEMCOLLECTOR;
        IBlockState up = itemCollector.getDefaultState().withProperty(BlockMachineBase.FACING, EnumFacing.UP);
        IBlockState north = itemCollector.getDefaultState().withProperty(BlockMachineBase.FACING, EnumFacing.NORTH);

        assertBounds(itemCollector.getBoundingBox(up, null, BlockPos.ORIGIN), 3, 5, 3, 13, 16, 13);
        assertBounds(itemCollector.getBoundingBox(north, null, BlockPos.ORIGIN), 3, 3, 0, 13, 13, 11);
    }

    @Test
    void itemCollectorDoesNotAdvertiseSolidFaces() {
        BlockItemCollector itemCollector = ModBlocks.ITEMCOLLECTOR;
        IBlockState state = itemCollector.getDefaultState();

        assertEquals(BlockFaceShape.UNDEFINED,
                itemCollector.getBlockFaceShape(null, state, BlockPos.ORIGIN, EnumFacing.UP),
                "Item Collector should expose undefined block faces for its sculpted model");
    }

    @Test
    void itemCollectorUsesCutoutRenderLayer() {
        assertEquals(BlockRenderLayer.CUTOUT_MIPPED, ModBlocks.ITEMCOLLECTOR.getRenderLayer());
    }

    private static void assertBounds(AxisAlignedBB box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        assertEquals(minX / 16.0D, box.minX, 0.0001D);
        assertEquals(minY / 16.0D, box.minY, 0.0001D);
        assertEquals(minZ / 16.0D, box.minZ, 0.0001D);
        assertEquals(maxX / 16.0D, box.maxX, 0.0001D);
        assertEquals(maxY / 16.0D, box.maxY, 0.0001D);
        assertEquals(maxZ / 16.0D, box.maxZ, 0.0001D);
        assertTrue(box.maxX - box.minX < 1.0D || box.maxY - box.minY < 1.0D || box.maxZ - box.minZ < 1.0D,
                "Item Collector bounds must not collapse back to a full 1x1x1 cube");
    }
}

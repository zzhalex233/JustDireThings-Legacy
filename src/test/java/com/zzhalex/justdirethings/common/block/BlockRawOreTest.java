package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.registry.ModContentBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockRawOreTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void rawOreBlocksAreNotRenderedAsFullOpaqueCubes() {
        Block rawOre = ModContentBlocks.RAW_ECLIPSEALLOY_ORE;

        assertFalse(rawOre.isFullCube(rawOre.getDefaultState()), "Raw ore should not render as a full cube");
        assertFalse(rawOre.isOpaqueCube(rawOre.getDefaultState()), "Raw ore should not render as an opaque cube");
    }

    @Test
    void rawOreBlocksExposeFacingStateLikeUpstream() {
        Block rawOre = ModContentBlocks.RAW_ECLIPSEALLOY_ORE;

        assertTrue(rawOre.getDefaultState().getPropertyKeys().stream().map(IProperty::getName).anyMatch("facing"::equals),
                "Raw ore should keep a facing property for its sculpted model");
    }

    @Test
    void rawOreBlocksDoNotAdvertiseSolidFaces() {
        BlockRawOre rawOre = (BlockRawOre) ModContentBlocks.RAW_ECLIPSEALLOY_ORE;

        assertFalse(rawOre.isFullBlock(rawOre.getDefaultState()), "Raw ore should not be treated as a full block");
        assertFalse(rawOre.causesSuffocation(rawOre.getDefaultState()), "Raw ore should not cause suffocation like a full cube");
        assertEquals(BlockFaceShape.UNDEFINED,
                rawOre.getBlockFaceShape(null, rawOre.getDefaultState(), BlockPos.ORIGIN, EnumFacing.UP),
                "Raw ore should expose an undefined block face shape");
    }
}

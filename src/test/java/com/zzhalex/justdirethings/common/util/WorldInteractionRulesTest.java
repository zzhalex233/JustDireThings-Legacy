package com.zzhalex.justdirethings.common.util;

import com.zzhalex.justdirethings.common.tile.machine.MachineActionHelper;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldInteractionRulesTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void orientPlacementStateUsesMachineFacingForDirectionalBlocks() {
        IBlockState oriented = WorldInteractionRules.orientPlacementState(Blocks.DISPENSER.getDefaultState(), EnumFacing.SOUTH, EnumFacing.NORTH);

        assertEquals(EnumFacing.SOUTH, oriented.getValue(BlockDispenser.FACING));
    }

    @Test
    void orientPlacementStateFallsBackToHorizontalFacingForHorizontalBlocks() {
        IBlockState oriented = WorldInteractionRules.orientPlacementState(Blocks.CHEST.getDefaultState(), EnumFacing.UP, EnumFacing.WEST);

        assertEquals(EnumFacing.WEST, oriented.getValue(BlockChest.FACING));
    }

    @Test
    void infiniteWaterRuleRequiresTwoHorizontalNeighbors() {
        assertTrue(WorldInteractionRules.isInfiniteWaterSource(true, 2));
        assertFalse(WorldInteractionRules.isInfiniteWaterSource(true, 1));
        assertFalse(WorldInteractionRules.isInfiniteWaterSource(false, 4));
    }

    @Test
    void machineActionHelperRecognisesSpecialPlacementItems() {
        assertTrue(MachineActionHelper.canAttemptPlacement(new ItemStack(Items.REDSTONE)));
        assertFalse(MachineActionHelper.canAttemptPlacement(new ItemStack(Items.DIAMOND)));
    }
}

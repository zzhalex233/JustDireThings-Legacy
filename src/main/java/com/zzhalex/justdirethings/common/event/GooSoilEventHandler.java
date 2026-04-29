package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.registry.ModContentBlocks;
import com.zzhalex.justdirethings.registry.ModEquipmentItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class GooSoilEventHandler {

    public static final GooSoilEventHandler INSTANCE = new GooSoilEventHandler();

    private GooSoilEventHandler() {
    }

    @SubscribeEvent
    public void onUseHoe(UseHoeEvent event) {
        ItemStack held = event.getCurrent();
        Block gooSoil = soilForHoe(held);
        if (gooSoil == null || !isEnabled(held)) {
            return;
        }

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if (!canTillIntoGooSoil(world, pos)) {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        if (!world.isRemote) {
            world.playSound(null, pos, SoundType.GROUND.getStepSound(), SoundCategory.BLOCKS, 1.0F, 0.8F);
            world.setBlockState(pos, gooSoil.getDefaultState(), 11);
            held.damageItem(1, player);
        }
        event.setResult(Event.Result.ALLOW);
    }

    private static Block soilForHoe(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        Item item = stack.getItem();
        if (item == ModEquipmentItems.getItem("ferricore_hoe")) {
            return ModContentBlocks.GOO_SOIL_TIER1;
        }
        if (item == ModEquipmentItems.getItem("blazegold_hoe")) {
            return ModContentBlocks.GOO_SOIL_TIER2;
        }
        if (item == ModEquipmentItems.getItem("celestigem_hoe")) {
            return ModContentBlocks.GOO_SOIL_TIER3;
        }
        if (item == ModEquipmentItems.getItem("eclipsealloy_hoe")) {
            return ModContentBlocks.GOO_SOIL_TIER4;
        }
        return null;
    }

    private static boolean isEnabled(ItemStack stack) {
        return !(stack.getItem() instanceof ToggleableTool) || ((ToggleableTool) stack.getItem()).isEnabled(stack);
    }

    private static boolean canTillIntoGooSoil(World world, BlockPos pos) {
        if (!world.isAirBlock(pos.up())) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.FARMLAND;
    }
}

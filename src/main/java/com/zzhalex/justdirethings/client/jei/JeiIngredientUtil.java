package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.common.recipe.custom.GooFluidRecipeRuntime;
import com.zzhalex.justdirethings.common.recipe.custom.JDTBlockStateSpec;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.Collections;
import java.util.List;

final class JeiIngredientUtil {

    private JeiIngredientUtil() {
    }

    static List<ItemStack> itemStacks(JDTBlockStateSpec spec) {
        return GooFluidRecipeRuntime.itemStacksForBlockState(spec);
    }

    static List<ItemStack> itemStacksForTag(String tag) {
        return GooFluidRecipeRuntime.itemStacksForBlockTag(tag);
    }

    static FluidStack fluidStack(JDTBlockStateSpec spec) {
        IBlockState state = spec.toBlockState();
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            Fluid fluid = ((IFluidBlock) block).getFluid();
            return fluid == null ? null : new FluidStack(fluid, 1000);
        }
        if ("minecraft:water".equals(spec.getBlockId().toString())) {
            return new FluidStack(FluidRegistry.WATER, 1000);
        }
        if ("minecraft:lava".equals(spec.getBlockId().toString())) {
            return new FluidStack(FluidRegistry.LAVA, 1000);
        }
        return null;
    }

    static List<ItemStack> gooCatalysts(int tierRequirement) {
        return GooFluidRecipeRuntime.gooCatalystsForTier(tierRequirement);
    }

    static List<ItemStack> singletonOrEmpty(ItemStack stack) {
        return stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack);
    }
}

package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.common.recipe.custom.FluidDropDataRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collections;
import java.util.List;

public class FluidDropRecipeWrapper implements IRecipeWrapper {

    private final FluidDropDataRecipe recipe;

    public FluidDropRecipeWrapper(FluidDropDataRecipe recipe) {
        this.recipe = recipe;
    }

    public FluidDropDataRecipe getRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        FluidStack inputFluid = JeiIngredientUtil.fluidStack(recipe.getInput());
        if (inputFluid != null) {
            ingredients.setInput(VanillaTypes.FLUID, inputFluid);
        }

        Item catalyst = ForgeRegistries.ITEMS.getValue(recipe.getCatalystId());
        if (catalyst != null) {
            ingredients.setInput(VanillaTypes.ITEM, new ItemStack(catalyst));
        }

        List<ItemStack> outputItems = JeiIngredientUtil.itemStacks(recipe.getOutput());
        if (!outputItems.isEmpty()) {
            ingredients.setOutput(VanillaTypes.ITEM, outputItems.get(0));
        }
        FluidStack outputFluid = JeiIngredientUtil.fluidStack(recipe.getOutput());
        if (outputFluid != null) {
            ingredients.setOutput(VanillaTypes.FLUID, outputFluid);
        }
    }
}

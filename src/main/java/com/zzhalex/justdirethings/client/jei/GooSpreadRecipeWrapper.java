package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadDataRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class GooSpreadRecipeWrapper implements IRecipeWrapper {

    private final GooSpreadDataRecipe recipe;

    public GooSpreadRecipeWrapper(GooSpreadDataRecipe recipe) {
        this.recipe = recipe;
    }

    public GooSpreadDataRecipe getRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> itemInputs = new ArrayList<>();
        List<ItemStack> inputItems = JeiIngredientUtil.itemStacks(recipe.getInput());
        if (!inputItems.isEmpty()) {
            itemInputs.add(inputItems);
        }
        List<ItemStack> catalysts = JeiIngredientUtil.gooCatalysts(recipe.getTierRequirement(), recipe.getCatalyst());
        if (!catalysts.isEmpty()) {
            itemInputs.add(catalysts);
        }
        if (!itemInputs.isEmpty()) {
            ingredients.setInputLists(VanillaTypes.ITEM, itemInputs);
        }

        FluidStack inputFluid = JeiIngredientUtil.fluidStack(recipe.getInput());
        if (inputFluid != null) {
            ingredients.setInput(VanillaTypes.FLUID, inputFluid);
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

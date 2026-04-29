package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadTagDataRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class GooSpreadTagRecipeWrapper implements IRecipeWrapper {

    private final GooSpreadTagDataRecipe recipe;

    public GooSpreadTagRecipeWrapper(GooSpreadTagDataRecipe recipe) {
        this.recipe = recipe;
    }

    public GooSpreadTagDataRecipe getRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> itemInputs = new ArrayList<>();
        itemInputs.add(JeiIngredientUtil.itemStacksForTag(recipe.getInputTag()));
        itemInputs.add(JeiIngredientUtil.gooCatalysts(recipe.getTierRequirement()));
        ingredients.setInputLists(VanillaTypes.ITEM, itemInputs);

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

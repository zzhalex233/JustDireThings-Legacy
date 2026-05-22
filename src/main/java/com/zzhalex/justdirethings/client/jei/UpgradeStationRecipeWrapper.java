package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.common.recipe.UpgradeStationRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UpgradeStationRecipeWrapper implements IRecipeWrapper {

    private final UpgradeStationRecipe recipe;

    public UpgradeStationRecipeWrapper(UpgradeStationRecipe recipe) {
        this.recipe = recipe;
    }

    public UpgradeStationRecipe getRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        if (!recipe.getTemplateStacks().isEmpty()) {
            inputs.add(recipe.getTemplateStacks());
        }
        inputs.add(recipe.getBaseStacks());
        inputs.add(recipe.getAdditionStacks());
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);

        ItemStack output = recipe.getJeiOutputStack();
        if (!output.isEmpty()) {
            ingredients.setOutput(VanillaTypes.ITEM, output);
        }
    }
}

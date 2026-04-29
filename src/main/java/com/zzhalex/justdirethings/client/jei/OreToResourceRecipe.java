package com.zzhalex.justdirethings.client.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class OreToResourceRecipe implements IRecipeWrapper {

    private final ItemStack oreBlock;
    private final ItemStack output;

    public OreToResourceRecipe(Block oreBlock, ItemStack output) {
        this.oreBlock = new ItemStack(oreBlock);
        this.output = output;
    }

    public ItemStack getOreBlock() {
        return oreBlock;
    }

    public ItemStack getOutput() {
        return output;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(ItemStack.class, oreBlock);
        ingredients.setOutput(ItemStack.class, output);
    }
}

package com.zzhalex.justdirethings.common.util;

import com.zzhalex.justdirethings.common.recipe.UpgradeStationRecipe;
import com.zzhalex.justdirethings.registry.ModRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariableProvider;

import javax.annotation.Nullable;
import java.util.List;

public class UpgradeStationComponentProcessor implements IComponentProcessor {

    private static final String EMPTY_ITEM = "minecraft:air";
    private UpgradeStationRecipe recipe;
    private String title;
    private String text;

    @Override
    public void setup(IVariableProvider<String> variables) {
        recipe = findRecipe(variables.has("recipe") ? variables.get("recipe") : "");
        title = variables.has("title") ? variables.get("title") : "";
        text = variables.has("text") ? variables.get("text") : "";
    }

    @Override
    public String process(String key) {
        if ("title".equals(key)) {
            return title;
        }
        if ("text".equals(key)) {
            return text;
        }
        if (recipe == null) {
            return EMPTY_ITEM;
        }
        if ("template".equals(key)) {
            return serialize(first(recipe.getTemplateStacks()));
        }
        if ("base".equals(key)) {
            return serialize(first(recipe.getBaseStacks()));
        }
        if ("addition".equals(key)) {
            return serialize(first(recipe.getAdditionStacks()));
        }
        if ("output".equals(key)) {
            return serialize(recipe.getJeiOutputStack());
        }
        return null;
    }

    @Nullable
    private static UpgradeStationRecipe findRecipe(String rawId) {
        if (rawId == null || rawId.isEmpty()) {
            return null;
        }
        String id = new ResourceLocation(rawId).getPath();
        for (UpgradeStationRecipe recipe : ModRecipes.UPGRADE_STATION_RECIPES) {
            if (recipe.getId().equals(id)) {
                return recipe;
            }
        }
        return null;
    }

    private static ItemStack first(List<ItemStack> stacks) {
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
    }

    private static String serialize(ItemStack stack) {
        return stack.isEmpty() || stack.getItem().getRegistryName() == null ? EMPTY_ITEM : stack.getItem().getRegistryName().toString();
    }
}

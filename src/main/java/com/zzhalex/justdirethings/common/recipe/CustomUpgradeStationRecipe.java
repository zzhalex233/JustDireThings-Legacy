package com.zzhalex.justdirethings.common.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collections;
import java.util.List;

public class CustomUpgradeStationRecipe extends UpgradeStationRecipe {

    private final ItemStack template;
    private final ItemStack base;
    private final ItemStack addition;
    private final ItemStack output;

    public CustomUpgradeStationRecipe(String id, ItemStack template, ItemStack base, ItemStack addition, ItemStack output) {
        super(id);
        this.template = template.copy();
        this.base = base.copy();
        this.addition = addition.copy();
        this.output = output.copy();
    }

    @Override
    public boolean matches(ItemStack template, ItemStack base, ItemStack addition) {
        return matchesStack(template, this.template)
                && matchesStack(base, this.base)
                && matchesStack(addition, this.addition);
    }

    @Override
    public ItemStack createOutputStack(ItemStack template, ItemStack base, ItemStack addition) {
        return output.copy();
    }

    @Override
    public List<ItemStack> getTemplateStacks() {
        return template.isEmpty() ? Collections.emptyList() : Collections.singletonList(template.copy());
    }

    @Override
    public List<ItemStack> getBaseStacks() {
        return base.isEmpty() ? Collections.emptyList() : Collections.singletonList(base.copy());
    }

    @Override
    public List<ItemStack> getAdditionStacks() {
        return addition.isEmpty() ? Collections.emptyList() : Collections.singletonList(addition.copy());
    }

    @Override
    public ItemStack getJeiOutputStack() {
        return output.copy();
    }

    @Override
    public com.zzhalex.justdirethings.data.tool.ToolState createOutput(com.zzhalex.justdirethings.data.tool.ToolState... inputs) {
        return inputs.length == 0 ? new com.zzhalex.justdirethings.data.tool.ToolState() : UpgradeRecipeLogic.copyState(inputs[0]);
    }

    private static boolean matchesStack(ItemStack stack, ItemStack expected) {
        if (expected.isEmpty()) {
            return stack.isEmpty();
        }
        return !stack.isEmpty()
                && stack.getItem() == expected.getItem()
                && (expected.getItemDamage() == OreDictionary.WILDCARD_VALUE || stack.getItemDamage() == expected.getItemDamage())
                && ItemStack.areItemStackTagsEqual(stack, expected);
    }
}

package com.zzhalex.justdirethings.common.recipe;

import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class PaxelFusionRecipe extends UpgradeStationRecipe {

    private final Item pickaxeItem;
    private final Item axeItem;
    private final Item shovelItem;
    private final Item resultItem;

    public PaxelFusionRecipe(String id) {
        this(id, null, null, null, null);
    }

    public PaxelFusionRecipe(String id, Item pickaxeItem, Item axeItem, Item shovelItem, Item resultItem) {
        super(id);
        this.pickaxeItem = pickaxeItem;
        this.axeItem = axeItem;
        this.shovelItem = shovelItem;
        this.resultItem = resultItem;
    }

    @Override
    public boolean matches(ItemStack template, ItemStack base, ItemStack addition) {
        return sameItem(template, pickaxeItem)
                && sameItem(base, axeItem)
                && sameItem(addition, shovelItem);
    }

    @Override
    public ItemStack createOutputStack(ItemStack template, ItemStack base, ItemStack addition) {
        if (resultItem == null) {
            return ItemStack.EMPTY;
        }
        ItemStack output = new ItemStack(resultItem);
        writeToolState(output, createOutput(readToolState(template), readToolState(base), readToolState(addition)));
        return output;
    }

    @Override
    public List<ItemStack> getTemplateStacks() {
        return stackList(pickaxeItem);
    }

    @Override
    public List<ItemStack> getBaseStacks() {
        return stackList(axeItem);
    }

    @Override
    public List<ItemStack> getAdditionStacks() {
        return stackList(shovelItem);
    }

    @Override
    public ItemStack getJeiOutputStack() {
        return resultItem == null ? ItemStack.EMPTY : new ItemStack(resultItem);
    }

    @Override
    public ToolState createOutput(ToolState... inputs) {
        return UpgradeRecipeLogic.fusePaxel(inputs[0], inputs[1], inputs[2]);
    }
}

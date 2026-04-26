package com.zzhalex.justdirethings.common.recipe;

import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TierUpgradeRecipe extends UpgradeStationRecipe {

    private final Item templateItem;
    private final Item baseItem;
    private final Item additionItem;
    private final Item resultItem;

    public TierUpgradeRecipe(String id) {
        this(id, null, null, null, null);
    }

    public TierUpgradeRecipe(String id, Item templateItem, Item baseItem, Item additionItem, Item resultItem) {
        super(id);
        this.templateItem = templateItem;
        this.baseItem = baseItem;
        this.additionItem = additionItem;
        this.resultItem = resultItem;
    }

    @Override
    public boolean matches(ItemStack template, ItemStack base, ItemStack addition) {
        return sameItem(template, templateItem)
                && sameItem(base, baseItem)
                && sameItem(addition, additionItem);
    }

    @Override
    public ItemStack createOutputStack(ItemStack template, ItemStack base, ItemStack addition) {
        ItemStack output = copyWithItem(base, resultItem);
        writeToolState(output, createOutput(readToolState(base)));
        return output;
    }

    @Override
    public ToolState createOutput(ToolState... inputs) {
        return UpgradeRecipeLogic.upgradeTier(inputs[0]);
    }
}

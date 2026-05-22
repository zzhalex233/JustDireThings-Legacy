package com.zzhalex.justdirethings.common.recipe;

import com.zzhalex.justdirethings.config.JDTConfig;
import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

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
        return templateMatches(template)
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
    public List<ItemStack> getTemplateStacks() {
        return usesSmithingTemplate() ? stackList(templateItem) : java.util.Collections.emptyList();
    }

    @Override
    public List<ItemStack> getBaseStacks() {
        return stackList(baseItem);
    }

    @Override
    public List<ItemStack> getAdditionStacks() {
        return stackList(additionItem);
    }

    @Override
    public ItemStack getJeiOutputStack() {
        return resultItem == null ? ItemStack.EMPTY : new ItemStack(resultItem);
    }

    @Override
    public ToolState createOutput(ToolState... inputs) {
        return UpgradeRecipeLogic.upgradeTier(inputs[0]);
    }

    @Override
    public boolean usesSmithingTemplate() {
        return JDTConfig.enableSmithingTemplates;
    }

    private boolean templateMatches(ItemStack template) {
        return usesSmithingTemplate() ? sameItem(template, templateItem) : template.isEmpty();
    }
}

package com.zzhalex.justdirethings.common.recipe;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

public class AbilityInstallRecipe extends UpgradeStationRecipe {

    private final String abilityId;
    private final Item upgradeItem;
    private final Set<Item> allowedBaseItems;

    public AbilityInstallRecipe(String id, String abilityId) {
        this(id, null, abilityId, Collections.emptySet());
    }

    public AbilityInstallRecipe(String id, Item upgradeItem, String abilityId, Collection<Item> allowedBaseItems) {
        super(id);
        this.upgradeItem = upgradeItem;
        this.abilityId = abilityId;
        this.allowedBaseItems = new LinkedHashSet<>(allowedBaseItems);
    }

    public String getAbilityId() {
        return abilityId;
    }

    @Override
    public boolean matches(ItemStack template, ItemStack base, ItemStack addition) {
        Ability ability = Ability.byId(abilityId);
        return template.isEmpty()
                && !base.isEmpty()
                && ability != null
                && ability.requiresUpgrade()
                && allowedBaseItems.contains(base.getItem())
                && sameItem(addition, upgradeItem)
                && base.getItem() instanceof ToggleableTool
                && ((ToggleableTool) base.getItem()).supportsAbility(ability)
                && !((ToggleableTool) base.getItem()).hasInstalledAbility(base, ability);
    }

    @Override
    public ItemStack createOutputStack(ItemStack template, ItemStack base, ItemStack addition) {
        ItemStack output = base.copy();
        output.setCount(1);
        writeToolState(output, createOutput(readToolState(base)));
        return output;
    }

    @Override
    public List<ItemStack> getBaseStacks() {
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
        for (Item item : allowedBaseItems) {
            if (item != null) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks;
    }

    @Override
    public List<ItemStack> getAdditionStacks() {
        return stackList(upgradeItem);
    }

    @Override
    public ItemStack getJeiOutputStack() {
        ItemStack base = firstOrEmpty(getBaseStacks());
        ItemStack addition = firstOrEmpty(getAdditionStacks());
        return base.isEmpty() || addition.isEmpty() ? ItemStack.EMPTY : createOutputStack(ItemStack.EMPTY, base, addition);
    }

    @Override
    public ToolState createOutput(ToolState... inputs) {
        return UpgradeRecipeLogic.installAbility(inputs[0], abilityId);
    }
}

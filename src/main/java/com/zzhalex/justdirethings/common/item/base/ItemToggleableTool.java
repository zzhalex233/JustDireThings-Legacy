package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.data.tool.ToolStateIO;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ItemToggleableTool extends Item {

    private final EnumSet<Ability> supportedAbilities = EnumSet.noneOf(Ability.class);

    protected ItemToggleableTool() {
        setMaxStackSize(1);
    }

    public Set<Ability> getSupportedAbilities() {
        if (supportedAbilities.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(supportedAbilities));
    }

    public boolean supportsAbility(Ability ability) {
        return ability != null && supportedAbilities.contains(ability);
    }

    public ToolState getToolState(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return createDefaultToolState();
        }

        NBTTagCompound rootTag = stack.getTagCompound();
        if (rootTag == null || !rootTag.hasKey(JDTDataKeys.TOOL_STATE, Constants.NBT.TAG_COMPOUND)) {
            return createDefaultToolState();
        }

        return ToolStateIO.read(rootTag.getCompoundTag(JDTDataKeys.TOOL_STATE));
    }

    public void setToolState(ItemStack stack, ToolState state) {
        if (stack == null || stack.isEmpty() || state == null) {
            return;
        }

        NBTTagCompound rootTag = stack.getTagCompound();
        if (rootTag == null) {
            rootTag = new NBTTagCompound();
        }

        rootTag.setTag(JDTDataKeys.TOOL_STATE, ToolStateIO.write(state));
        stack.setTagCompound(rootTag);
    }

    public void updateToolState(ItemStack stack, Consumer<ToolState> updater) {
        if (updater == null || stack == null || stack.isEmpty()) {
            return;
        }

        ToolState state = getToolState(stack);
        updater.accept(state);
        setToolState(stack, state);
    }

    public boolean isEnabled(ItemStack stack) {
        return getToolState(stack).isEnabled();
    }

    public void setEnabled(ItemStack stack, boolean enabled) {
        updateToolState(stack, state -> state.setEnabled(enabled));
    }

    public boolean hasInstalledAbility(ItemStack stack, Ability ability) {
        return ability != null && getToolState(stack).hasInstalledAbility(ability.getId());
    }

    public void installAbility(ItemStack stack, Ability ability) {
        if (ability == null) {
            return;
        }
        updateToolState(stack, state -> state.getInstalledAbilities().add(ability.getId()));
    }

    public int getAbilityValue(ItemStack stack, Ability ability, int defaultValue) {
        if (ability == null) {
            return defaultValue;
        }
        return getToolState(stack).getAbilityValues().getOrDefault(ability.getId(), defaultValue);
    }

    public void setAbilityValue(ItemStack stack, Ability ability, int value) {
        if (ability == null) {
            return;
        }
        updateToolState(stack, state -> state.getAbilityValues().put(ability.getId(), value));
    }

    protected final void addSupportedAbility(Ability ability) {
        if (ability != null) {
            supportedAbilities.add(ability);
        }
    }

    protected final void addSupportedAbilities(Ability... abilities) {
        supportedAbilities.addAll(Arrays.asList(abilities));
    }

    protected ToolState createDefaultToolState() {
        return new ToolState();
    }
}

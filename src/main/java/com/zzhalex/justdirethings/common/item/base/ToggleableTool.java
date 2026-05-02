package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.data.tool.ToolStateIO;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface ToggleableTool {

    Set<Ability> getSupportedAbilities();

    default Map<Ability, AbilityParams> getAbilityParamsMap() {
        return Collections.emptyMap();
    }

    default AbilityParams getAbilityParams(Ability ability) {
        return getAbilityParamsMap().getOrDefault(ability, new AbilityParams(-1, -1, 1));
    }

    default boolean supportsAbility(Ability ability) {
        return ability != null && getSupportedAbilities().contains(ability);
    }

    default ToolState getToolState(ItemStack stack) {
        return readToolState(stack);
    }

    default void setToolState(ItemStack stack, ToolState state) {
        writeToolState(stack, state);
    }

    default void updateToolState(ItemStack stack, Consumer<ToolState> updater) {
        if (updater == null || stack == null || stack.isEmpty()) {
            return;
        }

        ToolState state = getToolState(stack);
        updater.accept(state);
        setToolState(stack, state);
    }

    default boolean isEnabled(ItemStack stack) {
        return getToolState(stack).isEnabled();
    }

    default void setEnabled(ItemStack stack, boolean enabled) {
        updateToolState(stack, state -> state.setEnabled(enabled));
    }

    default boolean hasInstalledAbility(ItemStack stack, Ability ability) {
        return ability != null && hasUpgrade(stack, ability);
    }

    default void installAbility(ItemStack stack, Ability ability) {
        if (supportsAbility(ability)) {
            updateToolState(stack, state -> state.getInstalledAbilities().add(ability.getId()));
        }
    }

    default boolean getSetting(ItemStack stack, Ability ability) {
        return getSetting(stack, ability.getId());
    }

    default boolean getSetting(ItemStack stack, String abilityId) {
        return getToolState(stack).isEnabled()
                && !getToolState(stack).getAbilityValues().containsKey(settingDisabledKey(abilityId));
    }

    default void setSetting(ItemStack stack, Ability ability, boolean enabled) {
        setSetting(stack, ability.getId(), enabled);
    }

    default void setSetting(ItemStack stack, String abilityId, boolean enabled) {
        updateToolState(stack, state -> {
            if (enabled) {
                state.getAbilityValues().remove(settingDisabledKey(abilityId));
            } else {
                state.getAbilityValues().put(settingDisabledKey(abilityId), 1);
            }
        });
    }

    default void toggleSetting(ItemStack stack, Ability ability) {
        setSetting(stack, ability, !getSetting(stack, ability));
    }

    default void cycleSetting(ItemStack stack, Ability ability) {
        AbilityParams params = getAbilityParams(ability);
        int currentValue = getToolValue(stack, ability);
        int nextValue = Math.min(params.maxSlider, currentValue + params.increment);
        if (nextValue == currentValue && getSetting(stack, ability)) {
            setSetting(stack, ability, false);
            nextValue = params.minSlider;
        } else if (currentValue == params.minSlider && !getSetting(stack, ability)) {
            nextValue = params.minSlider;
            setSetting(stack, ability, true);
        }
        setToolValue(stack, ability, nextValue);
    }

    default void setToolValue(ItemStack stack, Ability ability, int value) {
        AbilityParams params = getAbilityParams(ability);
        int clamped = Math.max(params.minSlider, Math.min(params.maxSlider, value));
        updateToolState(stack, state -> state.getAbilityValues().put(ability.getId(), clamped));
    }

    default int getToolValue(ItemStack stack, Ability ability) {
        AbilityParams params = getAbilityParams(ability);
        int value = getToolState(stack).getAbilityValues().getOrDefault(ability.getId(), params.defaultValue);
        return Math.max(params.minSlider, Math.min(params.maxSlider, value));
    }

    default void setCustomSetting(ItemStack stack, Ability ability, int value) {
        updateToolState(stack, state -> state.getAbilityCustomSettings().put(ability.getId(), value));
    }

    default int getCustomSetting(ItemStack stack, Ability ability) {
        return getToolState(stack).getAbilityCustomSettings().getOrDefault(ability.getId(), 0);
    }

    static boolean hasUpgrade(ItemStack stack, Ability ability) {
        return ability != null && readToolState(stack).hasInstalledAbility(ability.getId());
    }

    static void addCooldown(ItemStack stack, Ability ability, int ticks, boolean active) {
        if (stack == null || stack.isEmpty() || ability == null || ticks <= 0) {
            return;
        }
        ToolState state = readToolState(stack);
        state.getAbilityCooldowns().removeIf(cooldown -> ability.getId().equals(cooldown.getAbilityId()));
        state.getAbilityCooldowns().add(new AbilityCooldown(ability.getId(), ticks, active));
        writeToolState(stack, state);
    }

    static ToolState readToolState(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.hasTagCompound()
                && stack.getTagCompound().hasKey(JDTDataKeys.TOOL_STATE, Constants.NBT.TAG_COMPOUND)) {
            return ToolStateIO.read(stack.getTagCompound().getCompoundTag(JDTDataKeys.TOOL_STATE));
        }
        return new ToolState();
    }

    static void writeToolState(ItemStack stack, ToolState state) {
        if (stack == null || stack.isEmpty() || state == null) {
            return;
        }

        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
        }
        root.setTag(JDTDataKeys.TOOL_STATE, ToolStateIO.write(state));
        stack.setTagCompound(root);
    }

    static String settingDisabledKey(String abilityId) {
        return abilityId + ":disabled";
    }
}

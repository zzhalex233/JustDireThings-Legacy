package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.data.tool.AbilityBinding;
import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface LeftClickableTool {

    static void setBindingMode(ItemStack stack, Ability ability, int mode) {
        if (ability == null) {
            return;
        }
        ToolState state = ToggleableTool.readToolState(stack);
        state.getAbilityBindingModes().put(ability.getId(), mode);
        ToggleableTool.writeToolState(stack, state);
    }

    static int getBindingMode(ItemStack stack, Ability ability) {
        if (ability == null) {
            return 0;
        }
        return ToggleableTool.readToolState(stack).getAbilityBindingModes().getOrDefault(ability.getId(), 0);
    }

    static void addToLeftClickList(ItemStack stack, Ability ability) {
        if (ability == null) {
            return;
        }
        ToolState state = ToggleableTool.readToolState(stack);
        if (!state.getLeftClickAbilities().contains(ability.getId())) {
            state.getLeftClickAbilities().add(ability.getId());
        }
        ToggleableTool.writeToolState(stack, state);
    }

    static void removeFromLeftClickList(ItemStack stack, Ability ability) {
        if (ability == null) {
            return;
        }
        ToolState state = ToggleableTool.readToolState(stack);
        state.getLeftClickAbilities().remove(ability.getId());
        ToggleableTool.writeToolState(stack, state);
    }

    static Set<Ability> getLeftClickList(ItemStack stack) {
        ToolState state = ToggleableTool.readToolState(stack);
        Set<Ability> abilities = new LinkedHashSet<>();
        for (String abilityId : state.getLeftClickAbilities()) {
            Ability ability = Ability.byId(abilityId);
            if (ability != null && getBindingMode(stack, ability) == 1) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    static AbilityBinding getAbilityBinding(ItemStack stack, Ability ability) {
        if (ability == null) {
            return null;
        }
        for (AbilityBinding binding : getCustomBindingList(stack)) {
            if (ability.getId().equals(binding.getAbilityId())) {
                return binding;
            }
        }
        return null;
    }

    static void addToCustomBindingList(ItemStack stack, AbilityBinding binding) {
        if (binding == null) {
            return;
        }
        Ability ability = Ability.byId(binding.getAbilityId());
        removeFromCustomBindingList(stack, ability);
        ToolState state = ToggleableTool.readToolState(stack);
        state.getAbilityBindings().add(binding);
        ToggleableTool.writeToolState(stack, state);
    }

    static void removeFromCustomBindingList(ItemStack stack, Ability ability) {
        if (ability == null) {
            return;
        }
        ToolState state = ToggleableTool.readToolState(stack);
        state.getAbilityBindings().removeIf(binding -> ability.getId().equals(binding.getAbilityId()));
        ToggleableTool.writeToolState(stack, state);
    }

    static List<AbilityBinding> getCustomBindingList(ItemStack stack) {
        return new ArrayList<>(ToggleableTool.readToolState(stack).getAbilityBindings());
    }
}

package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.data.tool.AbilityBinding;
import net.minecraft.item.ItemStack;

public final class ToolSettingApplier {

    private ToolSettingApplier() {
    }

    public static boolean applySlotSetting(ItemStack stack, String abilityId, int mode, int value) {
        Ability ability = Ability.byId(abilityId);
        if (ability == null || stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return false;
        }

        ToggleableTool tool = (ToggleableTool) stack.getItem();
        if (!tool.supportsAbility(ability)) {
            return false;
        }

        if (mode == 0) {
            tool.toggleSetting(stack, ability);
            return true;
        }
        if (mode == 1) {
            tool.cycleSetting(stack, ability);
            return true;
        }
        if (mode == 2) {
            tool.setToolValue(stack, ability, value);
            return true;
        }
        if (mode == 3) {
            tool.setCustomSetting(stack, ability, value);
            return true;
        }
        return false;
    }

    public static boolean applyBinding(ItemStack stack, String abilityId, int button, int keyCode, boolean isMouse, boolean requireEquipped) {
        Ability ability = Ability.byId(abilityId);
        if (ability == null || stack == null || stack.isEmpty() || !(stack.getItem() instanceof LeftClickableTool)) {
            return false;
        }

        LeftClickableTool.setBindingMode(stack, ability, button);
        if (button == 0) {
            LeftClickableTool.removeFromLeftClickList(stack, ability);
            return true;
        }
        if (button == 1) {
            LeftClickableTool.addToLeftClickList(stack, ability);
            return true;
        }
        if (button == 2) {
            if (keyCode == -1) {
                LeftClickableTool.removeFromCustomBindingList(stack, ability);
            } else {
                LeftClickableTool.addToCustomBindingList(stack, new AbilityBinding(ability.getId(), keyCode, isMouse, requireEquipped));
            }
            return true;
        }
        return false;
    }
}

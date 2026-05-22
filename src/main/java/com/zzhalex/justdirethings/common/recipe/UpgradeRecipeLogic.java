package com.zzhalex.justdirethings.common.recipe;

import com.zzhalex.justdirethings.data.tool.AbilityBinding;
import com.zzhalex.justdirethings.data.tool.ToolState;

public final class UpgradeRecipeLogic {

    private UpgradeRecipeLogic() {
    }

    public static ToolState installAbility(ToolState state, String abilityId) {
        ToolState copy = copyState(state);
        copy.getInstalledAbilities().add(abilityId);
        return copy;
    }

    public static ToolState upgradeTier(ToolState state) {
        return copyState(state);
    }

    public static ToolState fusePaxel(ToolState pickaxe, ToolState axe, ToolState shovel) {
        ToolState output = copyState(pickaxe);
        mergeInto(output, axe);
        mergeInto(output, shovel);
        return output;
    }

    public static ToolState copyState(ToolState source) {
        ToolState copy = new ToolState();
        copy.setEnabled(source.isEnabled());
        copy.getInstalledAbilities().addAll(source.getInstalledAbilities());
        copy.getAbilityValues().putAll(source.getAbilityValues());
        copy.getAbilityCustomSettings().putAll(source.getAbilityCustomSettings());
        copy.getAbilityBindingModes().putAll(source.getAbilityBindingModes());
        copy.getLeftClickAbilities().addAll(source.getLeftClickAbilities());

        for (AbilityBinding binding : source.getAbilityBindings()) {
            copy.getAbilityBindings().add(new AbilityBinding(
                    binding.getAbilityId(),
                    binding.getKeyCode(),
                    binding.isMouseBinding(),
                    binding.isRequireEquipped()
            ));
        }

        return copy;
    }

    private static void mergeInto(ToolState target, ToolState source) {
        target.getInstalledAbilities().addAll(source.getInstalledAbilities());
        target.getAbilityValues().putAll(source.getAbilityValues());
        target.getAbilityCustomSettings().putAll(source.getAbilityCustomSettings());
        target.getAbilityBindingModes().putAll(source.getAbilityBindingModes());

        for (String leftClickAbility : source.getLeftClickAbilities()) {
            if (!target.getLeftClickAbilities().contains(leftClickAbility)) {
                target.getLeftClickAbilities().add(leftClickAbility);
            }
        }

        for (AbilityBinding binding : source.getAbilityBindings()) {
            target.getAbilityBindings().add(new AbilityBinding(
                    binding.getAbilityId(),
                    binding.getKeyCode(),
                    binding.isMouseBinding(),
                    binding.isRequireEquipped()
            ));
        }

    }
}

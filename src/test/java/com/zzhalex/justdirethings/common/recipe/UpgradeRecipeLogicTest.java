package com.zzhalex.justdirethings.common.recipe;

import com.zzhalex.justdirethings.data.tool.ToolState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeRecipeLogicTest {

    @Test
    void abilityInstallPreservesExistingFlags() {
        ToolState state = new ToolState();
        state.setEnabled(false);

        ToolState output = UpgradeRecipeLogic.installAbility(state, "flight");

        assertFalse(output.isEnabled());
        assertTrue(output.hasInstalledAbility("flight"));
    }

    @Test
    void tierUpgradePreservesInstalledAbilities() {
        ToolState state = new ToolState();
        state.getInstalledAbilities().add("glowing");

        ToolState output = UpgradeRecipeLogic.upgradeTier(state);

        assertTrue(output.hasInstalledAbility("glowing"));
    }

    @Test
    void paxelFusionMergesInstalledAbilities() {
        ToolState pickaxe = new ToolState();
        pickaxe.getInstalledAbilities().add("flight");
        ToolState axe = new ToolState();
        axe.getInstalledAbilities().add("glowing");
        ToolState shovel = new ToolState();
        shovel.getInstalledAbilities().add("void_shift");

        ToolState output = UpgradeRecipeLogic.fusePaxel(pickaxe, axe, shovel);

        assertTrue(output.hasInstalledAbility("flight"));
        assertTrue(output.hasInstalledAbility("glowing"));
        assertTrue(output.hasInstalledAbility("void_shift"));
    }
}

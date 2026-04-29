package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.data.tool.AbilityBinding;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.registry.ModEquipmentItems;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolSettingStateTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void equipmentItemsExposeOriginalSupportedAbilities() {
        ToggleableTool ferricoreSword = assertInstanceOf(ToggleableTool.class, ModEquipmentItems.getItem("ferricore_sword"));
        ToggleableTool eclipseChestplate = assertInstanceOf(ToggleableTool.class, ModEquipmentItems.getItem("eclipsealloy_chestplate"));

        assertTrue(ferricoreSword.supportsAbility(Ability.MOBSCANNER));
        assertFalse(ferricoreSword.supportsAbility(Ability.FLIGHT));
        assertTrue(eclipseChestplate.supportsAbility(Ability.FLIGHT));
        assertTrue(eclipseChestplate.supportsAbility(Ability.DEATHPROTECTION));
    }

    @Test
    void cycleSettingMatchesOriginalDisableThenEnableFlow() {
        ItemStack pickaxe = new ItemStack(ModEquipmentItems.getItem("blazegold_pickaxe"));
        ToggleableTool tool = (ToggleableTool) pickaxe.getItem();

        assertEquals(3, tool.getToolValue(pickaxe, Ability.HAMMER));
        assertTrue(tool.getSetting(pickaxe, Ability.HAMMER));

        tool.cycleSetting(pickaxe, Ability.HAMMER);

        assertFalse(tool.getSetting(pickaxe, Ability.HAMMER));
        assertEquals(3, tool.getToolValue(pickaxe, Ability.HAMMER));

        tool.cycleSetting(pickaxe, Ability.HAMMER);

        assertTrue(tool.getSetting(pickaxe, Ability.HAMMER));
        assertEquals(3, tool.getToolValue(pickaxe, Ability.HAMMER));
    }

    @Test
    void leftClickAndCustomBindingsPersistInToolState() {
        ItemStack sword = new ItemStack(ModEquipmentItems.getItem("ferricore_sword"));

        LeftClickableTool.setBindingMode(sword, Ability.MOBSCANNER, 1);
        LeftClickableTool.addToLeftClickList(sword, Ability.MOBSCANNER);
        LeftClickableTool.addToCustomBindingList(sword, new AbilityBinding("mobscanner", 33, false, true));

        ToolState state = ToggleableTool.readToolState(sword);
        List<AbilityBinding> bindings = state.getAbilityBindings();

        assertEquals(1, state.getAbilityBindingModes().get("mobscanner"));
        assertEquals(List.of("mobscanner"), state.getLeftClickAbilities());
        assertEquals(1, bindings.size());
        assertEquals(33, bindings.get(0).getKeyCode());
        assertTrue(bindings.get(0).isRequireEquipped());
    }
}

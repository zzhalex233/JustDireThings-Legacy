package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.data.tool.AbilityBinding;
import com.zzhalex.justdirethings.registry.ModEquipmentItems;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolSettingApplierTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void appliesOriginalToolSlotPacketModesToNbt() {
        ItemStack pickaxe = new ItemStack(ModEquipmentItems.getItem("eclipsealloy_pickaxe"));

        ToolSettingApplier.applySlotSetting(pickaxe, "smelter", 0, -1);
        assertFalse(((ToggleableTool) pickaxe.getItem()).getSetting(pickaxe, Ability.SMELTER));

        ToolSettingApplier.applySlotSetting(pickaxe, "hammer", 1, -1);
        assertFalse(((ToggleableTool) pickaxe.getItem()).getSetting(pickaxe, Ability.HAMMER));

        ToolSettingApplier.applySlotSetting(pickaxe, "hammer", 1, -1);
        assertTrue(((ToggleableTool) pickaxe.getItem()).getSetting(pickaxe, Ability.HAMMER));

        ToolSettingApplier.applySlotSetting(pickaxe, "hammer", 2, 999);
        assertEquals(7, ((ToggleableTool) pickaxe.getItem()).getToolValue(pickaxe, Ability.HAMMER));

        ToolSettingApplier.applySlotSetting(pickaxe, "dropteleport", 3, 1);
        assertEquals(1, ((ToggleableTool) pickaxe.getItem()).getCustomSetting(pickaxe, Ability.DROPTELEPORT));
    }

    @Test
    void appliesOriginalLeftRightClickBindingPacketModesToNbt() {
        ItemStack sword = new ItemStack(ModEquipmentItems.getItem("ferricore_sword"));

        ToolSettingApplier.applyBinding(sword, "mobscanner", 1, -1, false, true);
        assertTrue(LeftClickableTool.getLeftClickList(sword).contains(Ability.MOBSCANNER));

        ToolSettingApplier.applyBinding(sword, "mobscanner", 2, 42, false, false);
        AbilityBinding binding = LeftClickableTool.getAbilityBinding(sword, Ability.MOBSCANNER);
        assertEquals(42, binding.getKeyCode());
        assertFalse(binding.isRequireEquipped());

        ToolSettingApplier.applyBinding(sword, "mobscanner", 0, -1, false, true);
        assertFalse(LeftClickableTool.getLeftClickList(sword).contains(Ability.MOBSCANNER));
    }

    @Test
    void preservesCustomBindingWhenOnlyRequireEquippedChanges() {
        ItemStack sword = new ItemStack(ModEquipmentItems.getItem("ferricore_sword"));

        ToolSettingApplier.applyBinding(sword, "mobscanner", 2, 42, false, true);
        ToolSettingApplier.applyBinding(sword, "mobscanner", 2, -1, false, false);

        AbilityBinding binding = LeftClickableTool.getAbilityBinding(sword, Ability.MOBSCANNER);
        assertEquals(42, binding.getKeyCode());
        assertFalse(binding.isMouseBinding());
        assertFalse(binding.isRequireEquipped());
    }
}

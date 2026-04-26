package com.zzhalex.justdirethings.common.recipe;

import com.zzhalex.justdirethings.common.tile.TileUpgradeStation;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.data.tool.ToolState;
import com.zzhalex.justdirethings.data.tool.ToolStateIO;
import com.zzhalex.justdirethings.registry.ModContentItems;
import com.zzhalex.justdirethings.registry.ModEquipmentItems;
import com.zzhalex.justdirethings.registry.ModRecipes;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeStationRecipeIntegrationTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void tierUpgradeProducesNextTierItemAndPreservesToolState() {
        ItemStack base = stack(ModEquipmentItems.getItem("ferricore_sword"));
        installAbility(base, "glowing");

        ItemStack output = ModRecipes.getUpgradeStationOutput(
                stack(ModContentItems.getItem("template_blazegold")),
                base,
                stack(ModContentItems.getItem("blazegold_ingot"))
        );

        assertFalse(output.isEmpty());
        assertEquals(ModEquipmentItems.getItem("blazegold_sword"), output.getItem());
        assertTrue(readState(output).hasInstalledAbility("glowing"));
    }

    @Test
    void abilityInstallAddsAbilityToOutputCopy() {
        ItemStack output = ModRecipes.getUpgradeStationOutput(
                ItemStack.EMPTY,
                stack(ModEquipmentItems.getItem("ferricore_sword")),
                stack(ModContentItems.getItem("upgrade_flight"))
        );

        assertFalse(output.isEmpty());
        assertEquals(ModEquipmentItems.getItem("ferricore_sword"), output.getItem());
        assertTrue(readState(output).hasInstalledAbility("flight"));
    }

    @Test
    void paxelFusionMergesToolStateFromAllInputs() {
        ItemStack pickaxe = stack(ModEquipmentItems.getItem("celestigem_pickaxe"));
        ItemStack axe = stack(ModEquipmentItems.getItem("celestigem_axe"));
        ItemStack shovel = stack(ModEquipmentItems.getItem("celestigem_shovel"));
        installAbility(pickaxe, "flight");
        installAbility(axe, "glowing");
        installAbility(shovel, "voidshift");

        ItemStack output = ModRecipes.getUpgradeStationOutput(pickaxe, axe, shovel);

        assertFalse(output.isEmpty());
        assertEquals(ModEquipmentItems.getItem("celestigem_paxel"), output.getItem());
        assertTrue(readState(output).hasInstalledAbility("flight"));
        assertTrue(readState(output).hasInstalledAbility("glowing"));
        assertTrue(readState(output).hasInstalledAbility("voidshift"));
    }

    @Test
    void tileRefreshesOutputFromInputsAndConsumesInputsWhenCrafted() {
        TileUpgradeStation tile = new TileUpgradeStation();
        tile.setStackInSlot(TileUpgradeStation.SLOT_TEMPLATE, stack(ModContentItems.getItem("template_blazegold")));
        tile.setStackInSlot(TileUpgradeStation.SLOT_BASE, stack(ModEquipmentItems.getItem("ferricore_sword")));
        tile.setStackInSlot(TileUpgradeStation.SLOT_ADDITION, stack(ModContentItems.getItem("blazegold_ingot")));

        tile.refreshOutput();

        assertEquals(ModEquipmentItems.getItem("blazegold_sword"), tile.getStackInSlot(TileUpgradeStation.SLOT_OUTPUT).getItem());

        tile.consumeInputsForOutput(tile.getStackInSlot(TileUpgradeStation.SLOT_OUTPUT));

        assertTrue(tile.getStackInSlot(TileUpgradeStation.SLOT_TEMPLATE).isEmpty());
        assertTrue(tile.getStackInSlot(TileUpgradeStation.SLOT_BASE).isEmpty());
        assertTrue(tile.getStackInSlot(TileUpgradeStation.SLOT_ADDITION).isEmpty());
        assertTrue(tile.getStackInSlot(TileUpgradeStation.SLOT_OUTPUT).isEmpty());
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }

    private static void installAbility(ItemStack stack, String abilityId) {
        ToolState state = readState(stack);
        state.getInstalledAbilities().add(abilityId);
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
        }
        root.setTag(JDTDataKeys.TOOL_STATE, ToolStateIO.write(state));
        stack.setTagCompound(root);
    }

    private static ToolState readState(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(JDTDataKeys.TOOL_STATE)) {
            return ToolStateIO.read(stack.getTagCompound().getCompoundTag(JDTDataKeys.TOOL_STATE));
        }
        return new ToolState();
    }
}

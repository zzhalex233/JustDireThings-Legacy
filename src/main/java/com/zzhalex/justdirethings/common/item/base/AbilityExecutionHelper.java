package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class AbilityExecutionHelper {

    private AbilityExecutionHelper() {
    }

    public static ActionResult<ItemStack> tryExecuteRightClickAbility(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return null;
        }

        ToggleableTool tool = (ToggleableTool) stack.getItem();
        ToolState state = ToggleableTool.readToolState(stack);
        boolean consumedAny = false;
        boolean voidShiftTakesPriority = canExecuteFromDirectUse(tool, stack, state, Ability.VOIDSHIFT)
                && AbilityMethods.canStartVoidShift(world, player, stack);
        for (Ability ability : tool.getSupportedAbilities()) {
            if (ability == Ability.AIRBURST && voidShiftTakesPriority) {
                continue;
            }
            if (!ability.requiresUseAction() || ability.getBindingType() != Ability.BindingType.LEFT_AND_CUSTOM
                    || !canExecuteFromDirectUse(tool, stack, state, ability)) {
                continue;
            }
            consumedAny |= AbilityMethods.execute(ability, world, player, stack);
        }
        return consumedAny ? new ActionResult<>(EnumActionResult.SUCCESS, stack) : null;
    }

    public static EnumActionResult tryExecuteUseOnAbility(
            World world,
            EntityPlayer player,
            EnumHand hand,
            BlockPos pos,
            EnumFacing facing
    ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return EnumActionResult.PASS;
        }

        ToggleableTool tool = (ToggleableTool) stack.getItem();
        ToolState state = ToggleableTool.readToolState(stack);
        boolean consumedAny = false;
        for (Ability ability : tool.getSupportedAbilities()) {
            if (!ability.requiresUseOnAction() || ability.getBindingType() != Ability.BindingType.LEFT_AND_CUSTOM
                    || !canExecuteFromDirectUseOn(tool, stack, state, ability)) {
                continue;
            }
            consumedAny |= AbilityMethods.executeUseOn(ability, world, player, stack, pos, facing, hand);
        }
        return consumedAny ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    public static boolean canExecuteFromDirectUse(ToggleableTool tool, ItemStack stack, ToolState state, Ability ability) {
        return tool != null
                && stack != null
                && !stack.isEmpty()
                && state != null
                && ability != null
                && tool.supportsAbility(ability)
                && tool.hasInstalledAbility(stack, ability)
                && tool.getSetting(stack, ability)
                && (!(stack.getItem() instanceof LeftClickableTool) || LeftClickableTool.getBindingMode(stack, ability) == 0);
    }

    public static boolean canExecuteFromDirectUseOn(ToggleableTool tool, ItemStack stack, ToolState state, Ability ability) {
        return tool != null
                && stack != null
                && !stack.isEmpty()
                && state != null
                && ability != null
                && tool.supportsAbility(ability)
                && tool.hasInstalledAbility(stack, ability)
                && tool.getSetting(stack, ability)
                && (!(stack.getItem() instanceof LeftClickableTool) || LeftClickableTool.getBindingMode(stack, ability) == 0);
    }
}

package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.AbilityExecutionHelper;
import com.zzhalex.justdirethings.common.item.base.ItemToggleableTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemBlazejetWand extends ItemToggleableTool {

    public static final int DURABILITY = 200;

    public ItemBlazejetWand() {
        setMaxDamage(DURABILITY);
        addSupportedAbility(Ability.LAVAREPAIR);
        addSupportedAbility(Ability.AIRBURST, new AbilityParams(1, 2, 1, 2));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ActionResult<ItemStack> abilityResult = AbilityExecutionHelper.tryExecuteRightClickAbility(world, player, hand);
        return abilityResult != null ? abilityResult : super.onItemRightClick(world, player, hand);
    }
}

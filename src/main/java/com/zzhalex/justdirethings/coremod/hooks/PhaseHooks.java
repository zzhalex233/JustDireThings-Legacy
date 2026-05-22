package com.zzhalex.justdirethings.coremod.hooks;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.common.block.group.JDTBlockGroups;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public final class PhaseHooks {

    private PhaseHooks() {
    }

    public static boolean canPhase(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }

        ItemStack leggings = ((EntityPlayer) entity).getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        return AbilityMethods.canUseAbility(leggings, Ability.PHASE);
    }

    public static void filterPhaseBlockCollisions(Entity entity, AxisAlignedBB collisionBox, List<AxisAlignedBB> boxes, World world) {
        if (!(entity instanceof EntityPlayer) || collisionBox == null || boxes == null || world == null || !canPhase(entity)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        Iterator<AxisAlignedBB> iterator = boxes.iterator();
        while (iterator.hasNext()) {
            AxisAlignedBB box = iterator.next();
            if (!shouldKeepPhaseCollision(player, collisionBox, box)) {
                iterator.remove();
            }
        }
    }

    private static boolean shouldKeepPhaseCollision(EntityPlayer player, AxisAlignedBB collisionBox, AxisAlignedBB blockBox) {
        BlockPos pos = new BlockPos(Math.floor(blockBox.minX), Math.floor(blockBox.minY), Math.floor(blockBox.minZ));
        IBlockState state = player.world.getBlockState(pos);
        if (isPhaseDenied(state, player.world, pos)) {
            return true;
        }

        // In the 1.12 collision hook, upward motion needs an explicit bypass or
        // players get "stuck" trying to jump while already phased into blocks.
        if (player.motionY > 1.0E-5D) {
            return false;
        }

        double verticalDelta = Math.abs(blockBox.maxY - collisionBox.minY);
        return verticalDelta < 0.75D;
    }

    private static boolean isPhaseDenied(IBlockState state, World world, BlockPos pos) {
        return state.getBlockHardness(world, pos) < 0.0F
                || JDTBlockGroups.isPhaseDenied(state.getBlock());
    }
}

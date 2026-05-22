package com.zzhalex.justdirethings.common.item.misc;

import com.zzhalex.justdirethings.common.entity.EntityCreatureCatcher;
import com.zzhalex.justdirethings.common.util.EntityDisplayNames;
import com.zzhalex.justdirethings.registry.ModItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemCreatureCatcher extends Item {

    public static final String TAG_CAPTURED_ENTITY_ID = "EntityType";
    public static final String TAG_CAPTURED_ENTITY_DATA = "EntityData";

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.ENTITY_SNOWBALL_THROW,
                SoundCategory.PLAYERS,
                0.5F,
                0.4F / (world.rand.nextFloat() * 0.4F + 0.8F)
        );

        if (!world.isRemote) {
            EntityCreatureCatcher projectile = new EntityCreatureCatcher(world, player);
            ItemStack returnStack = stack.copy();
            returnStack.setCount(1);
            projectile.setReturnStack(returnStack);
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            world.spawnEntity(projectile);
        }

        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static boolean hasEntity(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getTagCompound() != null
                && stack.getTagCompound().hasKey(TAG_CAPTURED_ENTITY_ID, Constants.NBT.TAG_STRING);
    }

    public static void setCapturedEntity(ItemStack stack, String entityId, NBTTagCompound entityData) {
        if (stack == null || stack.isEmpty() || entityId == null || entityId.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setString(TAG_CAPTURED_ENTITY_ID, entityId);
        if (entityData != null) {
            tag.setTag(TAG_CAPTURED_ENTITY_DATA, entityData.copy());
        }
    }

    public static String getCapturedEntityId(ItemStack stack) {
        if (!hasEntity(stack)) {
            return "";
        }
        return stack.getTagCompound().getString(TAG_CAPTURED_ENTITY_ID);
    }

    public static NBTTagCompound getCapturedEntityData(ItemStack stack) {
        if (!hasEntity(stack) || !stack.getTagCompound().hasKey(TAG_CAPTURED_ENTITY_DATA, Constants.NBT.TAG_COMPOUND)) {
            return new NBTTagCompound();
        }
        return stack.getTagCompound().getCompoundTag(TAG_CAPTURED_ENTITY_DATA).copy();
    }

    public static ItemStack createCapturedStack(String entityId, NBTTagCompound entityData) {
        ItemStack stack = new ItemStack(ModItems.CREATURE_CATCHER);
        setCapturedEntity(stack, entityId, entityData);
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (hasEntity(stack)) {
            tooltip.add(TextFormatting.DARK_GRAY
                    + I18n.format("justdirethings.creature")
                    + TextFormatting.GREEN
                    + getCapturedEntityName(stack));
        }
    }

    @SideOnly(Side.CLIENT)
    private static String getCapturedEntityName(ItemStack stack) {
        String entityId = getCapturedEntityId(stack);
        if (entityId.isEmpty()) {
            return entityId;
        }

        return EntityDisplayNames.translatedName(entityId);
    }
}

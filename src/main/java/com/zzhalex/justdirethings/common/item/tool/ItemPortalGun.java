package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.common.entity.EntityPortal;
import com.zzhalex.justdirethings.common.entity.EntityPortalProjectile;
import com.zzhalex.justdirethings.common.item.base.ItemPoweredTool;
import com.zzhalex.justdirethings.config.JDTConfig;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.UUID;

public class ItemPortalGun extends ItemPoweredTool {

    public ItemPortalGun() {
        super(JDTConfig.portalGunV1RfCapacity, JDTConfig.portalGunV1RfCapacity, JDTConfig.portalGunV1RfCapacity, 0);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                closePortals(stack, player);
            } else {
                spawnProjectile(world, player, stack, false);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public boolean spawnProjectile(World world, EntityPlayer player, ItemStack stack, boolean primary) {
        if (!player.capabilities.isCreativeMode && getStoredEnergy(stack) < JDTConfig.portalGunV1RfCost) {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.lowenergy"), true);
            return false;
        }

        if (!player.capabilities.isCreativeMode) {
            setStoredEnergy(stack, getStoredEnergy(stack) - JDTConfig.portalGunV1RfCost);
        }

        EntityPortalProjectile projectile = new EntityPortalProjectile(world, player, getOrCreatePortalGunId(stack), primary);
        projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.0F, 1.0F);
        world.spawnEntity(projectile);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 0.5F, 1.0F);
        return true;
    }

    public int closePortals(ItemStack stack, EntityPlayer player) {
        UUID portalGunId = getOrCreatePortalGunId(stack);
        int closed = 0;
        for (World loadedWorld : DimensionManager.getWorlds()) {
            for (Entity entity : loadedWorld.loadedEntityList) {
                if (entity instanceof EntityPortal) {
                    EntityPortal portal = (EntityPortal) entity;
                    if (portalGunId.equals(portal.getPortalGunUuid()) || (player != null && player.getUniqueID().equals(portal.getOwnerUuid()))) {
                        portal.markDying();
                        closed++;
                    }
                }
            }
        }
        return closed;
    }

    public UUID getOrCreatePortalGunId(ItemStack stack) {
        NBTTagCompound tag = getOrCreateTag(stack);
        if (tag.hasKey(JDTDataKeys.PORTAL_GUN_UUID)) {
            try {
                return UUID.fromString(tag.getString(JDTDataKeys.PORTAL_GUN_UUID));
            } catch (IllegalArgumentException ignored) {
                tag.removeTag(JDTDataKeys.PORTAL_GUN_UUID);
            }
        }
        UUID uuid = UUID.randomUUID();
        tag.setString(JDTDataKeys.PORTAL_GUN_UUID, uuid.toString());
        return uuid;
    }

    public static ItemStack findHeldPortalGun(EntityPlayer player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.getItem() instanceof ItemPortalGun) {
            return mainHand;
        }

        ItemStack offHand = player.getHeldItemOffhand();
        if (offHand.getItem() instanceof ItemPortalGun) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int getStoredEnergy(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.PORTAL_GUN_ENERGY);
    }

    @Override
    public void setStoredEnergy(ItemStack stack, int storedEnergy) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.PORTAL_GUN_ENERGY, Math.max(0, Math.min(getEnergyCapacity(stack), storedEnergy)));
    }

    @Override
    public int getEnergyCapacity(ItemStack stack) {
        return JDTConfig.portalGunV1RfCapacity;
    }

    @Override
    public int getMaxReceive(ItemStack stack) {
        return getEnergyCapacity(stack);
    }

    @Override
    public int getMaxExtract(ItemStack stack) {
        return getEnergyCapacity(stack);
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }
}

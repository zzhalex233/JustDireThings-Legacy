package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.misc.ItemTotemOfDeathRecall;
import com.zzhalex.justdirethings.registry.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class TotemOfDeathRecallEvents {

    private static final String PLAYER_DEATH_RECALL = "JustDireThingsDeathRecall";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_X = "X";
    private static final String TAG_Y = "Y";
    private static final String TAG_Z = "Z";

    private TotemOfDeathRecallEvents() {
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        ItemStack totem = findUnboundTotem(player);
        if (totem.isEmpty()) {
            return;
        }

        storeDeathLocation(player);
        totem.shrink(1);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getEntityPlayer() instanceof EntityPlayerMP)) {
            return;
        }

        NBTTagCompound deathLocation = event.getOriginal().getEntityData().getCompoundTag(PLAYER_DEATH_RECALL);
        if (deathLocation.isEmpty()) {
            return;
        }

        ItemStack boundTotem = new ItemStack(ModItems.TOTEM_OF_DEATH_RECALL);
        ItemTotemOfDeathRecall.setBoundLocation(
                boundTotem,
                deathLocation.getInteger(TAG_DIMENSION),
                deathLocation.getDouble(TAG_X),
                deathLocation.getDouble(TAG_Y),
                deathLocation.getDouble(TAG_Z)
        );
        event.getEntityPlayer().inventory.addItemStackToInventory(boundTotem);
    }

    private static ItemStack findUnboundTotem(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty()
                    && stack.getItem() == ModItems.TOTEM_OF_DEATH_RECALL
                    && !ItemTotemOfDeathRecall.hasBoundLocation(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void storeDeathLocation(EntityPlayerMP player) {
        NBTTagCompound deathLocation = new NBTTagCompound();
        deathLocation.setInteger(TAG_DIMENSION, player.dimension);
        deathLocation.setDouble(TAG_X, player.posX);
        deathLocation.setDouble(TAG_Y, player.posY);
        deathLocation.setDouble(TAG_Z, player.posZ);
        player.getEntityData().setTag(PLAYER_DEATH_RECALL, deathLocation);
    }
}

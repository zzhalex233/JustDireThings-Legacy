package com.zzhalex.justdirethings.client.render.tile;

import com.mojang.authlib.GameProfile;
import com.zzhalex.justdirethings.client.render.ClientPlayerPreviewRenderer;
import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import java.util.UUID;

@SideOnly(Side.CLIENT)
public class RenderInventoryHolder extends TileEntitySpecialRenderer<TileInventoryHolder> {

    private EntityOtherPlayerMP mockPlayer;
    private UUID mockPlayerUuid;

    @Override
    public void render(TileInventoryHolder tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile == null || tile.getWorld() == null || !tile.isRenderPlayer() || tile.getOwnerUuid() == null) {
            return;
        }

        EntityOtherPlayerMP player = getMockPlayer(tile.getOwnerUuid());
        if (player == null) {
            return;
        }

        equipMockPlayer(player, tile);
        ClientPlayerPreviewRenderer.renderInventoryHolderPlayer(player, x, y, z, partialTicks);
        unequipMockPlayer(player);
    }

    private EntityOtherPlayerMP getMockPlayer(UUID ownerUuid) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null) {
            return null;
        }
        if (mockPlayer == null || mockPlayerUuid == null || !mockPlayerUuid.equals(ownerUuid) || mockPlayer.world != minecraft.world) {
            mockPlayerUuid = ownerUuid;
            mockPlayer = new InventoryHolderPlayer(minecraft, new GameProfile(ownerUuid, "MockPlayer"));
        }
        return mockPlayer;
    }

    private void equipMockPlayer(EntityOtherPlayerMP player, TileInventoryHolder tile) {
        IItemHandler handler = tile.getItemHandler();
        player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, getStack(handler, tile.getRenderedSlot()));
        player.setHeldItem(net.minecraft.util.EnumHand.OFF_HAND, getStack(handler, 40));
        player.setItemStackToSlot(EntityEquipmentSlot.HEAD, getStack(handler, 36));
        player.setItemStackToSlot(EntityEquipmentSlot.CHEST, getStack(handler, 37));
        player.setItemStackToSlot(EntityEquipmentSlot.LEGS, getStack(handler, 38));
        player.setItemStackToSlot(EntityEquipmentSlot.FEET, getStack(handler, 39));
    }

    private void unequipMockPlayer(EntityOtherPlayerMP player) {
        player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, ItemStack.EMPTY);
        player.setHeldItem(net.minecraft.util.EnumHand.OFF_HAND, ItemStack.EMPTY);
        player.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
        player.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStack.EMPTY);
        player.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemStack.EMPTY);
        player.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemStack.EMPTY);
    }

    private ItemStack getStack(IItemHandler handler, int slot) {
        if (handler == null || slot < 0 || slot >= handler.getSlots()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = handler.getStackInSlot(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    private static class InventoryHolderPlayer extends EntityOtherPlayerMP {
        private final Minecraft minecraft;

        private InventoryHolderPlayer(Minecraft minecraft, GameProfile profile) {
            super(minecraft.world, profile);
            this.minecraft = minecraft;
        }

        @Override
        public boolean isWearing(EnumPlayerModelParts part) {
            AbstractClientPlayer currentPlayer = minecraft.player;
            if (currentPlayer != null && currentPlayer.getUniqueID().equals(getUniqueID())) {
                return currentPlayer.isWearing(part);
            }
            return part != EnumPlayerModelParts.CAPE;
        }
    }

}

package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class ContainerPocketGenerator extends Container {

    public static final int SLOT_FUEL = 0;
    public static final int SLOT_COUNT = 1;
    public static final int PLAYER_INV_START = 1;

    private final PocketGeneratorItem pocketGeneratorItem;
    private final ItemStack boundStack;
    private final IInventory fuelInventory;

    public ContainerPocketGenerator(InventoryPlayer playerInventory, ItemStack boundStack) {
        this.pocketGeneratorItem = (PocketGeneratorItem) boundStack.getItem();
        this.boundStack = boundStack;
        this.fuelInventory = new InventoryBasic("pocket_generator", false, SLOT_COUNT);
        this.fuelInventory.setInventorySlotContents(SLOT_FUEL, pocketGeneratorItem.getFuelStack(boundStack));

        addSlotToContainer(new Slot(fuelInventory, SLOT_FUEL, 80, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !stack.isEmpty()
                        && (TileEntityFurnace.isItemFuel(stack) || stack.getItem() instanceof FuelCanisterItem);
            }
        });

        addPlayerInventory(playerInventory);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return !boundStack.isEmpty() && playerIn.isEntityAlive();
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        pocketGeneratorItem.setFuelStack(boundStack, fuelInventory.getStackInSlot(SLOT_FUEL));
    }

    public int getStoredEnergy() {
        return pocketGeneratorItem.getStoredEnergy(boundStack);
    }

    public int getRemainingBurnTicks() {
        return pocketGeneratorItem.getCounter(boundStack);
    }

    public int getMaxBurnTicks() {
        return pocketGeneratorItem.getMaxBurn(boundStack);
    }

    private void addPlayerInventory(InventoryPlayer playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            addSlotToContainer(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
        }
    }
}

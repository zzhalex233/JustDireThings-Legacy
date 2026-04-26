package com.zzhalex.justdirethings.common.item.misc;

import com.zzhalex.justdirethings.capability.item.StackItemCapabilityProvider;
import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.compat.accessory.AccessoryInventoryBridge;
import com.zzhalex.justdirethings.common.item.base.EnergyBackedItem;
import com.zzhalex.justdirethings.config.JDTConfig;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class PocketGeneratorItem extends Item implements EnergyBackedItem {

    private static final int FE_PER_FUEL_TICK = 10;
    private static final int BASE_BURN_MULTIPLIER = 1;

    public PocketGeneratorItem() {
        setMaxStackSize(1);
        setTranslationKey(Reference.MOD_ID + ".pocket_generator");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (!world.isRemote) {
            player.openGui(JustDireThingsLegacy.INSTANCE, ModContainers.GUI_POCKET_GENERATOR, world, 0, 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, heldStack);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote || !(entity instanceof EntityPlayer) || !isEnabled(stack)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        tryBurn(stack);

        if (getStoredEnergy(stack) < Math.max(1, getFePerTick(stack) / 10)) {
            return;
        }

        for (ItemStack targetStack : player.inventory.mainInventory) {
            transferEnergy(stack, targetStack);
        }
        for (ItemStack targetStack : player.inventory.offHandInventory) {
            transferEnergy(stack, targetStack);
        }
        for (ItemStack targetStack : player.inventory.armorInventory) {
            transferEnergy(stack, targetStack);
        }

        AccessoryInventoryBridge accessoryBridge = AccessoryInventoryBridge.forPlayer(player);
        for (int slot = 0; slot < accessoryBridge.getSlotCount(); slot++) {
            transferEnergy(stack, accessoryBridge.getStackInSlot(slot));
        }
    }

    public boolean isEnabled(ItemStack stack) {
        NBTTagCompound tag = getOrCreateTag(stack);
        return !tag.hasKey(JDTDataKeys.TOOL_ENABLED) || tag.getBoolean(JDTDataKeys.TOOL_ENABLED);
    }

    public static int getEnabledModelState(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof PocketGeneratorItem)) {
            return 0;
        }

        PocketGeneratorItem item = (PocketGeneratorItem) stack.getItem();
        if (!item.isEnabled(stack)) {
            return 0;
        }

        return item.getStoredEnergy(stack) > 0 || item.getCounter(stack) > 0 ? 1 : 0;
    }

    public void setEnabled(ItemStack stack, boolean enabled) {
        getOrCreateTag(stack).setBoolean(JDTDataKeys.TOOL_ENABLED, enabled);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new StackItemCapabilityProvider(stack, this, null);
    }

    public int getStoredEnergy(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.POCKET_GENERATOR_ENERGY);
    }

    public void setStoredEnergy(ItemStack stack, int storedEnergy) {
        getOrCreateTag(stack).setInteger(
                JDTDataKeys.POCKET_GENERATOR_ENERGY,
                Math.max(0, Math.min(getMaxEnergy(), storedEnergy))
        );
    }

    public int getCounter(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.POCKET_GENERATOR_COUNTER);
    }

    public int getMaxBurn(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.POCKET_GENERATOR_MAX_BURN);
    }

    public int getFuelMultiplier(ItemStack stack) {
        return Math.max(1, getOrCreateTag(stack).getInteger(JDTDataKeys.POCKET_GENERATOR_FUEL_MULTIPLIER));
    }

    public void setFuelMultiplier(ItemStack stack, int multiplier) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.POCKET_GENERATOR_FUEL_MULTIPLIER, Math.max(1, multiplier));
    }

    public ItemStack getFuelStack(ItemStack stack) {
        NBTTagCompound tag = getOrCreateTag(stack);
        if (!tag.hasKey(JDTDataKeys.POCKET_GENERATOR_INVENTORY)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(tag.getCompoundTag(JDTDataKeys.POCKET_GENERATOR_INVENTORY));
    }

    public void setFuelStack(ItemStack stack, ItemStack fuelStack) {
        NBTTagCompound tag = getOrCreateTag(stack);
        if (fuelStack == null || fuelStack.isEmpty()) {
            tag.removeTag(JDTDataKeys.POCKET_GENERATOR_INVENTORY);
            return;
        }
        tag.setTag(JDTDataKeys.POCKET_GENERATOR_INVENTORY, fuelStack.serializeNBT());
    }

    public int getFePerTick(ItemStack stack) {
        return PocketGeneratorMath.fePerTick(FE_PER_FUEL_TICK, getBurnSpeedMultiplier(stack));
    }

    public int getMaxEnergy() {
        return JDTConfig.pocketGeneratorMaxFe;
    }

    @Override
    public int getEnergyCapacity(ItemStack stack) {
        return getMaxEnergy();
    }

    @Override
    public int getMaxReceive(ItemStack stack) {
        return FE_PER_FUEL_TICK;
    }

    @Override
    public int getMaxExtract(ItemStack stack) {
        return FE_PER_FUEL_TICK;
    }

    public int getBurnSpeedMultiplier(ItemStack stack) {
        return BASE_BURN_MULTIPLIER * getFuelMultiplier(stack);
    }

    public void tryBurn(ItemStack stack) {
        int counter = getCounter(stack);
        if (counter > 0 && getStoredEnergy(stack) < getMaxEnergy()) {
            burnTick(stack);
            return;
        }

        if (getStoredEnergy(stack) >= getMaxEnergy()) {
            return;
        }

        initBurn(stack);
    }

    private void burnTick(ItemStack stack) {
        int room = getMaxEnergy() - getStoredEnergy(stack);
        if (room <= 0) {
            return;
        }

        int generated = Math.min(room, getFePerTick(stack));
        setStoredEnergy(stack, getStoredEnergy(stack) + generated);

        int counter = Math.max(0, getCounter(stack) - 1);
        getOrCreateTag(stack).setInteger(JDTDataKeys.POCKET_GENERATOR_COUNTER, counter);

        if (counter <= 0) {
            getOrCreateTag(stack).setInteger(JDTDataKeys.POCKET_GENERATOR_MAX_BURN, 0);
        }
    }

    private boolean initBurn(ItemStack stack) {
        ItemStack fuelStack = getFuelStack(stack);
        if (fuelStack.isEmpty()) {
            return false;
        }

        int burnTime = TileEntityFurnace.getItemBurnTime(fuelStack);
        if (burnTime <= 0) {
            return false;
        }

        int fuelMultiplier = fuelStack.getItem() instanceof FuelCanisterItem
                ? FuelCanisterItem.getBurnSpeedMultiplier(fuelStack)
                : 1;
        setFuelMultiplier(stack, fuelMultiplier);

        if (fuelStack.getItem().hasContainerItem(fuelStack)) {
            setFuelStack(stack, fuelStack.getItem().getContainerItem(fuelStack));
        } else {
            fuelStack.shrink(1);
            setFuelStack(stack, fuelStack);
        }

        int counter = PocketGeneratorMath.burnTicksRemaining(burnTime, getBurnSpeedMultiplier(stack));
        NBTTagCompound tag = getOrCreateTag(stack);
        tag.setInteger(JDTDataKeys.POCKET_GENERATOR_COUNTER, counter);
        tag.setInteger(JDTDataKeys.POCKET_GENERATOR_MAX_BURN, counter);
        if (counter > 0) {
            burnTick(stack);
        }
        return counter > 0;
    }

    private void transferEnergy(ItemStack sourceStack, ItemStack targetStack) {
        if (targetStack == null || targetStack.isEmpty() || targetStack == sourceStack) {
            return;
        }

        IEnergyStorage targetStorage = targetStack.getCapability(CapabilityEnergy.ENERGY, null);
        if (targetStorage == null || !targetStorage.canReceive()) {
            return;
        }

        int availableEnergy = getStoredEnergy(sourceStack);
        if (availableEnergy <= 0) {
            return;
        }

        int maxTransfer = Math.min(availableEnergy, getFePerTick(sourceStack));
        int accepted = targetStorage.receiveEnergy(maxTransfer, true);
        if (accepted <= 0) {
            return;
        }

        int extracted = Math.min(availableEnergy, accepted);
        targetStorage.receiveEnergy(extracted, false);
        setStoredEnergy(sourceStack, availableEnergy - extracted);
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

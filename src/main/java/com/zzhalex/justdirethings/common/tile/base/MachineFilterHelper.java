package com.zzhalex.justdirethings.common.tile.base;

import com.zzhalex.justdirethings.common.entity.EntityCreatureCatcher;
import com.zzhalex.justdirethings.common.item.misc.ItemCreatureCatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public final class MachineFilterHelper {

    private MachineFilterHelper() {
    }

    public static boolean matchesFilter(IItemHandler filterHandler, MachineFilterState filterState, ItemStack stack) {
        if (filterHandler == null || filterState == null) {
            return true;
        }
        boolean allowList = filterState.isAllowList();
        for (int slot = 0; slot < filterHandler.getSlots(); slot++) {
            ItemStack filter = filterHandler.getStackInSlot(slot);
            if (!filter.isEmpty() && matchesFilterStack(filterState, filter, stack)) {
                return allowList;
            }
        }
        return !allowList;
    }

    public static boolean matchesFilterStack(MachineFilterState filterState, ItemStack filter, ItemStack stack) {
        if (filterState == null || filter.isEmpty() || stack.isEmpty()) {
            return false;
        }
        if (filter.getItem() != stack.getItem()) {
            return false;
        }
        if (filter.getMetadata() != stack.getMetadata()) {
            return false;
        }
        return !filterState.isCompareNbt() || ItemStack.areItemStackTagsEqual(filter, stack);
    }

    public static boolean matchesEntityFilter(IItemHandler filterHandler, MachineFilterState filterState, Entity entity, World world) {
        if (filterHandler == null || filterState == null) {
            return true;
        }
        boolean allowList = filterState.isAllowList();
        boolean hasFilter = false;
        for (int slot = 0; slot < filterHandler.getSlots(); slot++) {
            ItemStack filter = filterHandler.getStackInSlot(slot);
            if (filter.isEmpty()) {
                continue;
            }
            hasFilter = true;
            if (matchesEntityFilterStack(filterState, filter, entity, world)) {
                return allowList;
            }
        }
        return hasFilter ? !allowList : !allowList;
    }

    private static boolean matchesEntityFilterStack(MachineFilterState filterState, ItemStack filter, Entity entity, World world) {
        if (filter.getItem() instanceof ItemMonsterPlacer) {
            return matchesSpawnEgg(filter, entity);
        }
        if (filter.getItem() instanceof ItemCreatureCatcher) {
            return matchesCreatureCatcher(filterState, filter, entity, world);
        }
        return false;
    }

    private static boolean matchesSpawnEgg(ItemStack filter, Entity entity) {
        ResourceLocation eggEntityId = ItemMonsterPlacer.getNamedIdFrom(filter);
        ResourceLocation entityId = EntityList.getKey(entity);
        return eggEntityId != null && eggEntityId.equals(entityId);
    }

    private static boolean matchesCreatureCatcher(MachineFilterState filterState, ItemStack filter, Entity entity, World world) {
        String capturedId = ItemCreatureCatcher.getCapturedEntityId(filter);
        ResourceLocation entityId = EntityList.getKey(entity);
        if (capturedId.isEmpty() || entityId == null || !capturedId.equals(entityId.toString())) {
            return false;
        }
        if (!filterState.isCompareNbt()) {
            return true;
        }
        Entity captured = EntityCreatureCatcher.createCapturedEntity(filter, world);
        if (captured == null) {
            return false;
        }
        NBTTagCompound capturedTag = normalizedEntityTag(captured);
        NBTTagCompound targetTag = normalizedEntityTag(entity);
        return capturedTag.equals(targetTag);
    }

    private static NBTTagCompound normalizedEntityTag(Entity entity) {
        NBTTagCompound tag = new NBTTagCompound();
        entity.writeToNBT(tag);
        tag.removeTag("AbsorptionAmount");
        tag.removeTag("Age");
        tag.removeTag("Air");
        tag.removeTag("ArmorDropChances");
        tag.removeTag("ArmorItems");
        tag.removeTag("Attributes");
        tag.removeTag("CanPickUpLoot");
        tag.removeTag("DeathTime");
        tag.removeTag("Dimension");
        tag.removeTag("FallDistance");
        tag.removeTag("Fire");
        tag.removeTag("HandDropChances");
        tag.removeTag("HandItems");
        tag.removeTag("HurtByTimestamp");
        tag.removeTag("HurtTime");
        tag.removeTag("Invulnerable");
        tag.removeTag("Motion");
        tag.removeTag("OnGround");
        tag.removeTag("PortalCooldown");
        tag.removeTag("Pos");
        tag.removeTag("Rotation");
        tag.removeTag("UUIDLeast");
        tag.removeTag("UUIDMost");
        tag.removeTag("id");
        tag.removeTag("NoAI");
        tag.removeTag("Silent");
        tag.removeTag("Glowing");
        tag.removeTag("Tags");
        tag.removeTag("Leashed");
        tag.removeTag("Leash");
        tag.removeTag("CustomName");
        tag.removeTag("ActiveEffects");
        return tag;
    }
}

package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemJDTBow extends ItemBow {

    private final JDTToolTier tier;

    public ItemJDTBow(String id, JDTToolTier tier, int durability) {
        this.tier = tier;
        setMaxDamage(durability);
        EquipmentItemSupport.configure(this, id);
        addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "pull"), (stack, worldIn, entityIn) -> getPullValue(stack, entityIn));
        addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "pulling"), (stack, worldIn, entityIn) -> isPulling(stack, entityIn) ? 1.0F : 0.0F);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, tier.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }

    private float getPullValue(ItemStack stack, EntityLivingBase entity) {
        if (!isPulling(stack, entity)) {
            return 0.0F;
        }
        return (stack.getMaxItemUseDuration() - entity.getItemInUseCount()) / 20.0F;
    }

    private boolean isPulling(ItemStack stack, EntityLivingBase entity) {
        return entity != null && entity.isHandActive() && !entity.getActiveItemStack().isEmpty() && entity.getActiveItemStack().getItem() == this;
    }
}

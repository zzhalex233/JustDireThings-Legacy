package com.zzhalex.justdirethings.common.item.material;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModContentItems;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;

import java.util.function.Supplier;

public enum JDTArmorMaterial {
    FERRICORE("ferricore", 15, new int[]{2, 6, 5, 2}, 9, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0.0F, () -> repairStackOrEmpty("ferricore_ingot")),
    BLAZEGOLD("blazegold", 7, new int[]{2, 6, 5, 2}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 0.0F, () -> repairStackOrEmpty("blazegold_ingot")),
    CELESTIGEM("celestigem", 33, new int[]{3, 8, 6, 3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2.0F, () -> repairStackOrEmpty("celestigem")),
    ECLIPSEALLOY("eclipsealloy", 37, new int[]{3, 8, 6, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 3.0F, () -> repairStackOrEmpty("eclipsealloy_ingot"));

    private final String id;
    private final ItemArmor.ArmorMaterial vanillaMaterial;
    private final Supplier<ItemStack> repairStackSupplier;

    JDTArmorMaterial(
            String id,
            int durability,
            int[] reductionAmounts,
            int enchantability,
            SoundEvent equipSound,
            float toughness,
            Supplier<ItemStack> repairStackSupplier
    ) {
        this.id = id;
        this.vanillaMaterial = EnumHelper.addArmorMaterial(
                "JDT_" + name(),
                Reference.MOD_ID + ":" + id,
                durability,
                reductionAmounts,
                enchantability,
                equipSound,
                toughness
        );
        this.repairStackSupplier = repairStackSupplier;
    }

    public String getId() {
        return id;
    }

    public ItemArmor.ArmorMaterial asVanillaMaterial() {
        return vanillaMaterial;
    }

    public int getDamageReductionAmount(EntityEquipmentSlot slot) {
        return vanillaMaterial.getDamageReductionAmount(slot);
    }

    public ItemStack getRepairStack() {
        return repairStackSupplier.get().copy();
    }

    private static ItemStack repairStackOrEmpty(String itemId) {
        Item item = ModContentItems.getItem(itemId);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}

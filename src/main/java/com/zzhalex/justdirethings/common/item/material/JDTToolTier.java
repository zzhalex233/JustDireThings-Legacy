package com.zzhalex.justdirethings.common.item.material;

import com.zzhalex.justdirethings.registry.ModContentItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

import java.util.function.Supplier;

public enum JDTToolTier {
    FERRICORE("ferricore", 2, 500, 7.0F, 2.5F, 15, () -> repairStackOrEmpty("ferricore_ingot")),
    BLAZEGOLD("blazegold", 3, 1440, 12.0F, 3.0F, 22, () -> repairStackOrEmpty("blazegold_ingot")),
    CELESTIGEM("celestigem", 3, 1561, 10.0F, 4.0F, 18, () -> repairStackOrEmpty("celestigem")),
    ECLIPSEALLOY("eclipsealloy", 4, 2561, 16.0F, 5.0F, 25, () -> repairStackOrEmpty("eclipsealloy_ingot"));

    private final String id;
    private final Item.ToolMaterial vanillaMaterial;
    private final Supplier<ItemStack> repairStackSupplier;

    JDTToolTier(
            String id,
            int harvestLevel,
            int maxUses,
            float efficiency,
            float attackDamage,
            int enchantability,
            Supplier<ItemStack> repairStackSupplier
    ) {
        this.id = id;
        this.vanillaMaterial = EnumHelper.addToolMaterial(
                "JDT_" + name(),
                harvestLevel,
                maxUses,
                efficiency,
                attackDamage,
                enchantability
        );
        this.repairStackSupplier = repairStackSupplier;
    }

    public String getId() {
        return id;
    }

    public Item.ToolMaterial asVanillaMaterial() {
        return vanillaMaterial;
    }

    public ItemStack getRepairStack() {
        return repairStackSupplier.get().copy();
    }

    private static ItemStack repairStackOrEmpty(String itemId) {
        Item item = ModContentItems.getItem(itemId);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}

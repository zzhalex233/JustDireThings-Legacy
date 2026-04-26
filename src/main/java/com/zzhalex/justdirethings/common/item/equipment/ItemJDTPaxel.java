package com.zzhalex.justdirethings.common.item.equipment;

import com.google.common.collect.ImmutableSet;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

public class ItemJDTPaxel extends ItemTool {

    private static final ImmutableSet<Material> AXE_LIKE_MATERIALS = ImmutableSet.of(Material.WOOD, Material.PLANTS, Material.VINE);
    private final JDTToolTier tier;

    public ItemJDTPaxel(String id, JDTToolTier tier, float attackDamage, float attackSpeed) {
        super(attackDamage, attackSpeed, tier.asVanillaMaterial(), ImmutableSet.of());
        this.tier = tier;
        EquipmentItemSupport.configure(this, id);
    }

    @Override
    public boolean canHarvestBlock(IBlockState state) {
        return isSupportedHarvestTool(state) || AXE_LIKE_MATERIALS.contains(state.getMaterial());
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return canHarvestBlock(state) ? efficiency : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, tier.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }

    private boolean isSupportedHarvestTool(IBlockState state) {
        Block block = state.getBlock();
        String harvestTool = block.getHarvestTool(state);
        return "pickaxe".equals(harvestTool) || "axe".equals(harvestTool) || "shovel".equals(harvestTool);
    }
}

package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemJDTBow extends ItemBow implements ToggleableTool, LeftClickableTool {

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

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ActionResult<ItemStack> opened = EquipmentItemSupport.openSettingsIfSneaking(this, worldIn, playerIn, handIn);
        return opened != null ? opened : super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return EquipmentItemSupport.initEnergyCapabilities(this, stack);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EquipmentItemSupport.showEnergyBar(this, stack) || super.showDurabilityBar(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return EquipmentItemSupport.showEnergyBar(this, stack)
                ? EquipmentItemSupport.getEnergyDurabilityForDisplay(this, stack)
                : super.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return EquipmentItemSupport.showEnergyBar(this, stack)
                ? EquipmentItemSupport.getEnergyBarColor(this, stack)
                : super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        EquipmentItemSupport.appendEquipmentTooltip(this, stack, tooltip);
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

    @Override
    public Set<Ability> getSupportedAbilities() {
        return EquipmentItemSupport.getAbilities(this);
    }

    @Override
    public Map<Ability, AbilityParams> getAbilityParamsMap() {
        return EquipmentItemSupport.getAbilityParams(this);
    }
}

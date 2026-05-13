package com.zzhalex.justdirethings.common.item.equipment;

import com.google.common.collect.Multimap;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.common.item.base.AbilityExecutionHelper;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemJDTSword extends ItemSword implements ToggleableTool, LeftClickableTool {

    private final JDTToolTier tier;

    public ItemJDTSword(String id, JDTToolTier tier) {
        super(tier.asVanillaMaterial());
        this.tier = tier;
        EquipmentItemSupport.configure(this, id);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, tier.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ActionResult<ItemStack> opened = EquipmentItemSupport.openSettingsIfSneaking(this, worldIn, playerIn, handIn);
        if (opened != null) {
            return opened;
        }
        ActionResult<ItemStack> abilityResult = AbilityExecutionHelper.tryExecuteRightClickAbility(worldIn, playerIn, handIn);
        return abilityResult != null ? abilityResult : super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (EquipmentItemSupport.isCreativePlayer(attacker)) {
            return true;
        }
        if (EquipmentItemSupport.isPowered(stack) && !EquipmentItemSupport.hasPoweredDurability(stack, 1)) {
            return true;
        }
        if (EquipmentItemSupport.consumePoweredDurability(stack, 1)) {
            return true;
        }
        return super.hitEntity(stack, target, attacker);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (EquipmentItemSupport.isCreativePlayer(entityLiving)) {
            return true;
        }
        if (EquipmentItemSupport.isPowered(stack) && !EquipmentItemSupport.hasPoweredDurability(stack, 2)) {
            return true;
        }
        if (state.getBlockHardness(worldIn, pos) != 0.0F && EquipmentItemSupport.consumePoweredDurability(stack, 2)) {
            return true;
        }
        return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (EquipmentItemSupport.bindDrops(player, worldIn, pos, hand, facing)) {
            return EnumActionResult.SUCCESS;
        }
        EnumActionResult abilityResult = AbilityExecutionHelper.tryExecuteUseOnAbility(worldIn, player, hand, pos, facing);
        return abilityResult == EnumActionResult.SUCCESS ? abilityResult : super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        return EquipmentItemSupport.getPoweredAttributeModifiers(slot, stack, super.getAttributeModifiers(slot, stack));
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

    @Override
    public Set<Ability> getSupportedAbilities() {
        return EquipmentItemSupport.getAbilities(this);
    }

    @Override
    public Map<Ability, AbilityParams> getAbilityParamsMap() {
        return EquipmentItemSupport.getAbilityParams(this);
    }
}

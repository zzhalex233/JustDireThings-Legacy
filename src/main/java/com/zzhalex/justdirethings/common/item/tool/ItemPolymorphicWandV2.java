package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.common.item.base.AbilityExecutionHelper;
import com.zzhalex.justdirethings.common.item.base.FluidPickupHelper;
import com.zzhalex.justdirethings.common.item.base.ItemFluidPoweredTool;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import com.zzhalex.justdirethings.common.util.EntityDisplayNames;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModFluids;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPolymorphicWandV2 extends ItemFluidPoweredTool implements LeftClickableTool {

    public ItemPolymorphicWandV2() {
        super(1_000_000, 1_000_000, 1_000_000, 50, 8_000);
        setTranslationKey(Reference.MOD_ID + ".polymorphic_wand_v2");
        addSupportedAbilities(Ability.POLYMORPH_RANDOM, Ability.POLYMORPH_TARGET);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (FluidPickupHelper.pickupSourceFluid(world, player, stack, rayTrace(world, player, true), getContainedFluid(stack))) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (player.isSneaking()) {
            Entity lookedAt = AbilityMethods.getLookedAtEntity(world, player, 4.0D);
            if (lookedAt instanceof EntityLivingBase) {
                if (!world.isRemote) {
                    savePolymorphTarget(stack, player, (EntityLivingBase) lookedAt);
                }
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }
        ActionResult<ItemStack> settingsResult = openSettingsIfSneaking(world, player, hand);
        if (settingsResult != null) {
            return settingsResult;
        }
        ActionResult<ItemStack> abilityResult = AbilityExecutionHelper.tryExecuteRightClickAbility(world, player, hand);
        return abilityResult != null ? abilityResult : super.onItemRightClick(world, player, hand);
    }

    public static void savePolymorphTarget(ItemStack stack, EntityPlayer player, EntityLivingBase interactionTarget) {
        String entityId = getEntityId(interactionTarget);
        if (interactionTarget instanceof EntityLiving && !entityId.isEmpty() && !AbilityMethods.isPolymorphicTargetDenied(entityId)) {
            getOrCreateTag(stack).setString(JDTDataKeys.POLYMORPHIC_TARGET_ENTITY, entityId);
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.polymorphset", EntityDisplayNames.translationComponent(entityId)), true);
        } else {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.invalidpolymorphentity"), true);
        }
    }

    private static String getEntityId(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        return id == null ? "" : id.toString();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TextFormatting.DARK_AQUA + I18n.format(
                "justdirethings.polymorphicfluidamt",
                TooltipHelper.formatNumber(getStoredFluid(stack)),
                TooltipHelper.formatNumber(getFluidCapacity(stack))
        ));
        String entityId = getSavedPolymorphTarget(stack);
        if (!entityId.isEmpty()) {
            tooltip.add(TextFormatting.AQUA + I18n.format("justdirethings.polymorphset", EntityDisplayNames.translatedName(entityId)));
        }
    }

    @Override
    public int getStoredEnergy(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.POLYMORPHIC_WAND_ENERGY);
    }

    @Override
    public void setStoredEnergy(ItemStack stack, int storedEnergy) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.POLYMORPHIC_WAND_ENERGY, Math.max(0, Math.min(getEnergyCapacity(stack), storedEnergy)));
    }

    @Override
    public int getStoredFluid(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.POLYMORPHIC_WAND_FLUID);
    }

    @Override
    public void setStoredFluid(ItemStack stack, int storedFluid) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.POLYMORPHIC_WAND_FLUID, Math.max(0, Math.min(getFluidCapacity(stack), storedFluid)));
    }

    @Override
    public Fluid getContainedFluid(ItemStack stack) {
        ModFluids.bootstrap();
        return ModFluids.getFluid("polymorphic_fluid");
    }

    @Override
    public boolean canFillFluid(ItemStack stack, FluidStack resource) {
        return resource != null && resource.amount > 0 && resource.getFluid() == getContainedFluid(stack);
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    private static String getSavedPolymorphTarget(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return "";
        }
        return stack.getTagCompound().getString(JDTDataKeys.POLYMORPHIC_TARGET_ENTITY);
    }

}

package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.entity.EntityJustDireArrow;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.common.item.base.AbilityExecutionHelper;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemJDTBow extends ItemBow implements ToggleableTool, LeftClickableTool {

    private final JDTToolTier tier;
    private ItemStack activeBowStack = ItemStack.EMPTY;
    private ItemStack activeAmmoStack = ItemStack.EMPTY;
    private EntityLivingBase activeShooter;

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
        if (opened != null) {
            return opened;
        }
        ActionResult<ItemStack> abilityResult = AbilityExecutionHelper.tryExecuteRightClickAbility(worldIn, playerIn, handIn);
        return abilityResult != null ? abilityResult : super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        activeBowStack = stack;
        activeAmmoStack = getActiveAmmoStack(stack, entityLiving);
        activeShooter = entityLiving;
        try {
            super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
        } finally {
            activeBowStack = ItemStack.EMPTY;
            activeAmmoStack = ItemStack.EMPTY;
            activeShooter = null;
        }
    }

    @Override
    public EntityArrow customizeArrow(EntityArrow arrow) {
        if (activeShooter == null || activeBowStack.isEmpty() || activeAmmoStack.isEmpty() || activeAmmoStack.getItem() != Items.ARROW) {
            return super.customizeArrow(arrow);
        }

        EntityLivingBase shooter = activeShooter;
        ItemStack bowStack = activeBowStack;
        EntityJustDireArrow justDireArrow = new EntityJustDireArrow(shooter.world, shooter);
        justDireArrow.pickupStatus = arrow.pickupStatus;
        justDireArrow.setDamage(arrow.getDamage());
        justDireArrow.setKnockbackStrength(extractKnockback(arrow));
        if (arrow.getIsCritical()) {
            justDireArrow.setIsCritical(true);
        }
        if (isEpicArrowPrimed(bowStack)) {
            justDireArrow.setEpicArrow(true);
            justDireArrow.setDamage(20.0D);
            int cooldownTicks = getAbilityParams(Ability.EPICARROW).cooldown;
            ToggleableTool.addCooldown(bowStack, Ability.EPICARROW, cooldownTicks, false);
            setEpicArrowPrimed(bowStack, false);
        }
        applyPotionCanisterEffects(bowStack, justDireArrow);
        return super.customizeArrow(justDireArrow);
    }

    public boolean applyPotionCanisterEffects(ItemStack bowStack, EntityJustDireArrow arrow) {
        if (bowStack.isEmpty() || arrow == null || noPotionAbilitiesActive(bowStack)) {
            return false;
        }

        IItemHandler itemHandler = bowStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (itemHandler == null) {
            return false;
        }

        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack canister = itemHandler.getStackInSlot(slot);
            if (!(canister.getItem() instanceof PotionCanisterItem)) {
                continue;
            }

            PotionType potionType = PotionCanisterItem.getPotionType(canister);
            if (potionType == PotionTypes.EMPTY) {
                continue;
            }

            int neededAmount = getNeededPotionAmount(bowStack);
            if (PotionCanisterItem.getPotionAmount(canister) < neededAmount) {
                continue;
            }

            for (PotionEffect potionEffect : potionType.getEffects()) {
                arrow.addEffect(new PotionEffect(potionEffect));
            }
            PotionCanisterItem.setPotionAmount(canister, PotionCanisterItem.getPotionAmount(canister) - neededAmount);
            if (itemHandler instanceof net.minecraftforge.items.IItemHandlerModifiable) {
                ((net.minecraftforge.items.IItemHandlerModifiable) itemHandler).setStackInSlot(slot, canister);
            }
            applyPotionFlags(bowStack, arrow);
            return true;
        }
        return false;
    }

    private ItemStack getActiveAmmoStack(ItemStack bowStack, EntityLivingBase shooter) {
        if (!(shooter instanceof EntityPlayer)) {
            return ItemStack.EMPTY;
        }
        EntityPlayer player = (EntityPlayer) shooter;
        ItemStack ammoStack = findAmmo(player);
        if (!ammoStack.isEmpty()) {
            return ammoStack;
        }
        if (player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, bowStack) > 0) {
            return new ItemStack(Items.ARROW);
        }
        return ItemStack.EMPTY;
    }

    private boolean noPotionAbilitiesActive(ItemStack bowStack) {
        return !isActive(bowStack, Ability.POTIONARROW)
                && !isActive(bowStack, Ability.SPLASH)
                && !isActive(bowStack, Ability.LINGERING);
    }

    private int getNeededPotionAmount(ItemStack bowStack) {
        int neededAmount = 0;
        if (isActive(bowStack, Ability.POTIONARROW)) {
            neededAmount += 25;
        }
        if (isActive(bowStack, Ability.SPLASH)) {
            neededAmount += 25;
        }
        if (isActive(bowStack, Ability.LINGERING)) {
            neededAmount += 50;
        }
        return neededAmount;
    }

    private void applyPotionFlags(ItemStack bowStack, EntityJustDireArrow arrow) {
        if (isActive(bowStack, Ability.POTIONARROW)) {
            arrow.setPotionArrow(true);
        }
        if (isActive(bowStack, Ability.SPLASH)) {
            arrow.setSplash(true);
        }
        if (isActive(bowStack, Ability.LINGERING)) {
            arrow.setLingering(true);
        }
    }

    private boolean isActive(ItemStack bowStack, Ability ability) {
        return supportsAbility(ability) && hasInstalledAbility(bowStack, ability) && getSetting(bowStack, ability);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return EquipmentItemSupport.initBowCapabilities(this, stack);
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

    public static boolean isEpicArrowPrimed(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().getBoolean(JDTDataKeys.EPIC_ARROW);
    }

    public static void setEpicArrowPrimed(ItemStack stack, boolean primed) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setBoolean(JDTDataKeys.EPIC_ARROW, primed);
    }

    private int extractKnockback(EntityArrow arrow) {
        NBTTagCompound tag = new NBTTagCompound();
        arrow.writeEntityToNBT(tag);
        return tag.getByte("knockbackStrength");
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

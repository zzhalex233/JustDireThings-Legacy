package com.zzhalex.justdirethings.common.item;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemTimeCrystal extends ItemSimpleContent {

    private static final float EFFECT_CHANCE = 0.005F;
    private static final int EFFECT_DURATION_TICKS = 100;
    private static final int EFFECT_AMPLIFIER = 5;

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
        if (worldIn.isRemote || !(entityIn instanceof EntityLivingBase) || worldIn.rand.nextFloat() >= EFFECT_CHANCE) {
            return;
        }

        EntityLivingBase living = (EntityLivingBase) entityIn;
        if (timeProtection(entityIn)) {
            return;
        }

        if (worldIn.rand.nextBoolean()) {
            if (!living.isPotionActive(MobEffects.SPEED)) {
                living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER, false, false));
            }
        } else if (!living.isPotionActive(MobEffects.SLOWNESS)) {
            living.addPotionEffect(new PotionEffect(MobEffects.SPEED, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER, false, false));
        }
    }

    private boolean timeProtection(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }

        EntityPlayer player = (EntityPlayer) entity;
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.isEmpty() || !(chest.getItem() instanceof ToggleableTool)
                || !AbilityMethods.canUseAbilityAndDurability(chest, Ability.TIMEPROTECTION)) {
            return false;
        }

        AbilityMethods.damageTool(chest, player, Ability.TIMEPROTECTION);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String key = (System.currentTimeMillis() / 10000L) % 2L == 0L
                ? "justdirethings.timecrystaltooltip"
                : "justdirethings.timecrystaltooltiptwo";
        tooltip.add(TextFormatting.DARK_AQUA + I18n.format(key));
    }
}

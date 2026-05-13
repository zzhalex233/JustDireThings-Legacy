package com.zzhalex.justdirethings.common.item.misc;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.capability.item.StackItemCapabilityProvider;
import com.zzhalex.justdirethings.common.container.handler.PotionCanisterHandler;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class PotionCanisterItem extends Item {

    public static final int MAX_MB = 1000;

    public PotionCanisterItem() {
        setMaxStackSize(1);
        setTranslationKey(Reference.MOD_ID + ".potion_canister");
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new StackItemCapabilityProvider(stack, null, null, new PotionCanisterHandler(stack, JDTDataKeys.TOOL_CONTENTS, 1));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (!world.isRemote) {
            player.openGui(JustDireThingsLegacy.INSTANCE, ModContainers.GUI_POTION_CANISTER, world, 0, 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, heldStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        PotionType potionType = getPotionType(stack);
        int amount = getPotionAmount(stack);
        tooltip.add(TextFormatting.DARK_AQUA
                + I18n.format("justdirethings.fluidamt")
                + TextFormatting.GREEN
                + TooltipHelper.formatNumber(amount)
                + "/"
                + TooltipHelper.formatNumber(MAX_MB));
        if (potionType != PotionTypes.EMPTY && amount > 0) {
            ItemStack potionStack = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), potionType);
            PotionUtils.addPotionTooltip(potionStack, tooltip, 1.0F);
        }
    }

    public static PotionType getPotionType(ItemStack stack) {
        NBTTagCompound tag = getOrCreateTag(stack);
        String potionId = tag.getString(JDTDataKeys.POTION_CANISTER_POTION_ID);
        if (potionId.isEmpty()) {
            return PotionTypes.EMPTY;
        }
        PotionType potionType = PotionType.REGISTRY.getObject(new ResourceLocation(potionId));
        return potionType == null ? PotionTypes.EMPTY : potionType;
    }

    public static void setPotionType(ItemStack stack, PotionType potionType) {
        getOrCreateTag(stack).setString(
                JDTDataKeys.POTION_CANISTER_POTION_ID,
                potionType == null || potionType == PotionTypes.EMPTY ? "" : potionType.getRegistryName().toString()
        );
    }

    public static int getPotionAmount(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.POTION_CANISTER_AMOUNT);
    }

    public static int getFullness(ItemStack stack) {
        int potionAmount = getPotionAmount(stack);
        if (potionAmount <= 0) {
            return 0;
        }
        if (potionAmount <= 250) {
            return 1;
        }
        if (potionAmount <= 500) {
            return 2;
        }
        if (potionAmount <= 750) {
            return 3;
        }
        return 4;
    }

    public static void setPotionAmount(ItemStack stack, int amount) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.POTION_CANISTER_AMOUNT, Math.max(0, Math.min(MAX_MB, amount)));
        if (getPotionAmount(stack) == 0) {
            setPotionType(stack, PotionTypes.EMPTY);
        }
    }

    public static void addPotionAmount(ItemStack stack, int amount) {
        setPotionAmount(stack, getPotionAmount(stack) + amount);
    }

    public static boolean tryFillFromPotionItem(ItemStack canister, ItemStack potionStack) {
        if (canister.isEmpty() || potionStack.isEmpty() || !(potionStack.getItem() instanceof ItemPotion)) {
            return false;
        }

        PotionType incomingPotion = PotionUtils.getPotionFromItem(potionStack);
        PotionType currentPotion = getPotionType(canister);
        if (incomingPotion == PotionTypes.EMPTY) {
            return false;
        }
        if (currentPotion != PotionTypes.EMPTY && currentPotion != incomingPotion) {
            return false;
        }
        if (getPotionAmount(canister) + 250 > MAX_MB) {
            return false;
        }

        setPotionType(canister, incomingPotion);
        addPotionAmount(canister, 250);
        potionStack.shrink(1);
        return true;
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }
}

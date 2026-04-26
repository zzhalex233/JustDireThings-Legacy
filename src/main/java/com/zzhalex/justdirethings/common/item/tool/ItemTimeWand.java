package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.entity.EntityTimeWand;
import com.zzhalex.justdirethings.common.item.base.ItemFluidPoweredTool;
import com.zzhalex.justdirethings.common.util.TickAccelerationRules;
import com.zzhalex.justdirethings.config.JDTConfig;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModFluids;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;

public class ItemTimeWand extends ItemFluidPoweredTool {

    public ItemTimeWand() {
        super(100_000, 1_000, 1_000, 50, 8_000);
        setTranslationKey(Reference.MOD_ID + ".time_wand");
    }

    @Override
    public EnumActionResult onItemUse(
            EntityPlayer player,
            World world,
            BlockPos pos,
            EnumHand hand,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ
    ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!JDTConfig.timeWandFakePlayerAllowed && player instanceof FakePlayer) {
            return EnumActionResult.FAIL;
        }
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        return useOnBlock(world, player, pos, stack) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

    public boolean useOnBlock(World world, EntityPlayer player, BlockPos pos, ItemStack stack) {
        if (!canAccelerate(world, pos)) {
            return false;
        }

        EntityTimeWand existing = findExistingEntity(world, pos);
        int targetLevel = existing == null ? 1 : TickAccelerationRules.nextLevel(existing.getTickLevel());
        if (targetLevel < 0) {
            return false;
        }
        if (!hasResources(player, stack, targetLevel)) {
            return false;
        }

        consumeResources(player, stack, targetLevel);
        if (existing == null) {
            EntityTimeWand entity = new EntityTimeWand(world, pos);
            world.spawnEntity(entity);
        } else {
            existing.setTickLevel(targetLevel);
            existing.addBonusTime();
        }
        playLevelSound(world, pos, targetLevel);
        return true;
    }

    public int getStoredEnergy(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.TIME_WAND_ENERGY);
    }

    public void setStoredEnergy(ItemStack stack, int storedEnergy) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.TIME_WAND_ENERGY, Math.max(0, Math.min(JDTConfig.timeWandRfCapacity, storedEnergy)));
    }

    public int getStoredFluid(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.TIME_WAND_FLUID);
    }

    public void setStoredFluid(ItemStack stack, int storedFluid) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.TIME_WAND_FLUID, Math.max(0, Math.min(getFluidCapacity(), storedFluid)));
    }

    @Override
    public Fluid getContainedFluid(ItemStack stack) {
        ModFluids.bootstrap();
        return ModFluids.getFluid("time_fluid");
    }

    @Override
    public boolean canFillFluid(ItemStack stack, FluidStack resource) {
        return resource != null && resource.amount > 0 && resource.getFluid() == getContainedFluid(stack);
    }

    public boolean hasResources(EntityPlayer player, ItemStack stack, int targetLevel) {
        int requiredEnergy = TickAccelerationRules.feCostForLevel(targetLevel, player.capabilities.isCreativeMode);
        int requiredFluid = TickAccelerationRules.fluidCostForLevel(targetLevel, player.capabilities.isCreativeMode);

        if (getStoredEnergy(stack) < requiredEnergy) {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.lowenergy"), true);
            return false;
        }
        if (getStoredFluid(stack) < requiredFluid) {
            player.sendStatusMessage(new TextComponentTranslation("justdirethings.lowtimefluid"), true);
            return false;
        }
        return true;
    }

    private void consumeResources(EntityPlayer player, ItemStack stack, int targetLevel) {
        if (player.capabilities.isCreativeMode) {
            return;
        }
        setStoredEnergy(stack, getStoredEnergy(stack) - TickAccelerationRules.feCostForLevel(targetLevel, false));
        setStoredFluid(stack, getStoredFluid(stack) - TickAccelerationRules.fluidCostForLevel(targetLevel, false));
    }

    private boolean canAccelerate(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof net.minecraft.util.ITickable || state.getBlock().getTickRandomly();
    }

    private EntityTimeWand findExistingEntity(World world, BlockPos pos) {
        List<EntityTimeWand> entities = world.getEntitiesWithinAABB(EntityTimeWand.class, new AxisAlignedBB(pos));
        for (EntityTimeWand entity : entities) {
            if (pos.equals(entity.getAcceleratedPos())) {
                return entity;
            }
        }
        return null;
    }

    private void playLevelSound(World world, BlockPos pos, int level) {
        world.playSound(null, pos, SoundEvents.BLOCK_NOTE_XYLOPHONE, SoundCategory.PLAYERS, 1.0F, TickAccelerationRules.pitchForLevel(level));
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

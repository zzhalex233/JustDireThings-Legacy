package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.fluid.ExperienceHolderFluidTank;
import com.zzhalex.justdirethings.common.block.machine.BlockMachineBase;
import com.zzhalex.justdirethings.common.tile.base.MachineRedstoneState;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.common.util.ExperienceUtils;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageItemFlowParticle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;
import java.util.List;

public class TileExperienceHolder extends TileMachineBase implements ITickable {

    private int storedExperience;
    private int targetExperience;
    private EntityPlayer currentPlayer;
    private boolean ownerOnly;
    private boolean collectExperience;
    private boolean showParticles = true;
    private final IFluidHandler experienceFluidTank = new ExperienceHolderFluidTank(this);

    public TileExperienceHolder() {
        setTickSpeed(20);
        getRedstoneState().setMode(MachineRedstoneState.RedstoneMode.PULSE);
        getFluidState().setCapacity(Integer.MAX_VALUE);
        getAreaState().setOffset(0, 1, 0);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        handleTicks();
        evaluateRedstoneControl();

        boolean changed = false;
        if (collectExperience && getOperationTicks() == 0) {
            changed |= collectExperienceOrbs();
        }
        changed |= handleExperience();
        if (changed) {
            markDirtyClient();
        }
    }

    public int getStoredExperience() {
        return storedExperience;
    }

    public int getTargetExperience() {
        return targetExperience;
    }

    public void setTargetExperience(int targetExperience) {
        this.targetExperience = Math.max(0, targetExperience);
    }

    public boolean isOwnerOnly() {
        return ownerOnly;
    }

    public void setOwnerOnly(boolean ownerOnly) {
        this.ownerOnly = ownerOnly;
    }

    public boolean isCollectExperience() {
        return collectExperience;
    }

    public void setCollectExperience(boolean collectExperience) {
        this.collectExperience = collectExperience;
    }

    public boolean isShowParticles() {
        return showParticles;
    }

    public void setShowParticles(boolean showParticles) {
        this.showParticles = showParticles;
    }

    @Override
    public void setDirection(int direction) {
        EnumFacing oldFacing = EnumFacing.byIndex(getDirection()).getOpposite();
        boolean usingDefaultArea = getAreaState().getXRadius() == 0.0D
                && getAreaState().getYRadius() == 0.0D
                && getAreaState().getZRadius() == 0.0D
                && (isAreaOffsetUnset() || isAreaOffset(oldFacing));
        super.setDirection(direction);
        EnumFacing facing = EnumFacing.byIndex(getDirection()).getOpposite();
        if (usingDefaultArea) {
            getAreaState().setOffset(facing.getXOffset(), facing.getYOffset(), facing.getZOffset());
        }
    }

    private boolean isAreaOffsetUnset() {
        return getAreaState().getXOffset() == 0 && getAreaState().getYOffset() == 0 && getAreaState().getZOffset() == 0;
    }

    private boolean isAreaOffset(EnumFacing facing) {
        return getAreaState().getXOffset() == facing.getXOffset()
                && getAreaState().getYOffset() == facing.getYOffset()
                && getAreaState().getZOffset() == facing.getZOffset();
    }

    public int addExperience(int amount) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = Integer.MAX_VALUE - storedExperience < amount ? Integer.MAX_VALUE - storedExperience : amount;
        storedExperience += accepted;
        return amount - accepted;
    }

    public int subExperience(int amount) {
        int removed = Math.min(storedExperience, Math.max(0, amount));
        storedExperience -= removed;
        return amount - removed;
    }

    public void storeExperience(EntityPlayer player, int levels) {
        if (!canInteract(player)) {
            return;
        }
        if (levels == -1) {
            int totalExperience = ExperienceUtils.getPlayerTotalExperience(player);
            int remaining = addExperience(totalExperience);
            player.addExperience(-totalExperience);
            player.addExperienceLevel(-1);
            if (remaining > 0) {
                player.addExperience(remaining);
            }
        } else if (levels > 0) {
            int expInCurrentLevel = (int) (player.experience * player.xpBarCap());
            if (player.experience > 0.0F) {
                int removed = ExperienceUtils.removePoints(player, expInCurrentLevel);
                int remaining = addExperience(removed);
                levels--;
                player.experience = 0.0F;
                if (remaining > 0) {
                    player.addExperience(remaining);
                }
            }
            if (levels > 0) {
                int removed = ExperienceUtils.removeLevels(player, levels);
                int remaining = addExperience(removed);
                if (remaining > 0) {
                    player.addExperience(remaining);
                }
            }
        }
    }

    public void extractExperience(EntityPlayer player, int levels) {
        if (!canInteract(player) || storedExperience <= 0) {
            return;
        }
        if (levels == -1) {
            int expToGive = storedExperience;
            player.addExperience(expToGive);
            storedExperience = 0;
        } else if (levels > 0) {
            if (roundUpToNextLevel(player)) {
                levels--;
            }
            if (levels > 0 && storedExperience > 0) {
                int expForNextLevels = ExperienceUtils.getTotalExperienceForLevel(player.experienceLevel + levels) - ExperienceUtils.getPlayerTotalExperience(player);
                int expToGive = Math.min(storedExperience, expForNextLevels);
                player.addExperience(expToGive);
                storedExperience -= expToGive;
                roundUpToNextLevel(player);
            }
        }
    }

    private boolean collectExperienceOrbs() {
        AxisAlignedBB area = getAreaState().createArea(pos);
        List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(EntityXPOrb.class, area);
        boolean changed = false;
        for (EntityXPOrb orb : orbs) {
            addExperience(orb.xpValue);
            doParticles(new Vec3d(orb.posX, orb.posY, orb.posZ), true);
            orb.setDead();
            changed = true;
        }
        return changed;
    }

    private boolean handleExperience() {
        if (currentPlayer == null && isRedstoneActive() && canRun()) {
            currentPlayer = findTargetPlayer();
        }
        if (currentPlayer != null && (currentPlayer.isDead || !canInteract(currentPlayer))) {
            currentPlayer = null;
        }
        if (currentPlayer == null) {
            return false;
        }

        int before = storedExperience;
        int currentLevel = currentPlayer.experienceLevel;
        if (currentLevel < targetExperience && storedExperience > 0) {
            extractExperience(currentPlayer, 1);
            doParticles(playerParticleSource(currentPlayer), false);
            if (storedExperience == 0) {
                currentPlayer = null;
            }
        } else if (currentLevel > targetExperience || (currentLevel == targetExperience && currentPlayer.experience > 0.01F)) {
            storeExperience(currentPlayer, 1);
            doParticles(playerParticleSource(currentPlayer), true);
        } else {
            currentPlayer = null;
        }
        return before != storedExperience;
    }

    private Vec3d playerParticleSource(EntityPlayer player) {
        return new Vec3d(player.posX, player.posY + player.getEyeHeight() - 0.25D, player.posZ);
    }

    private void doParticles(Vec3d sourcePos, boolean toBlock) {
        if (!showParticles || world == null || world.isRemote) {
            return;
        }
        EnumFacing direction = getParticleFacing();
        Vec3d baubleSpot = new Vec3d(
                pos.getX() + 0.5D - 0.3D * direction.getXOffset(),
                pos.getY() + 0.5D - 0.3D * direction.getYOffset(),
                pos.getZ() + 0.5D - 0.3D * direction.getZOffset()
        );
        Vec3d start = toBlock ? sourcePos : baubleSpot;
        Vec3d target = toBlock ? baubleSpot : sourcePos;
        MessageItemFlowParticle message = new MessageItemFlowParticle(
                start.x,
                start.y,
                start.z,
                target.x,
                target.y,
                target.z,
                new ItemStack(Items.EXPERIENCE_BOTTLE),
                1
        );
        JDTNetwork.getChannel().sendToAllAround(message, new NetworkRegistry.TargetPoint(world.provider.getDimension(), start.x, start.y, start.z, 64.0D));
    }

    private EnumFacing getParticleFacing() {
        if (world != null && pos != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getPropertyKeys().contains(BlockMachineBase.FACING)) {
                return state.getValue(BlockMachineBase.FACING);
            }
        }
        return EnumFacing.byIndex(getDirection());
    }

    private EntityPlayer findTargetPlayer() {
        AxisAlignedBB area = getAreaState().createArea(pos);
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, area);
        for (EntityPlayer player : players) {
            if (!canInteract(player)) {
                continue;
            }
            if (player.experienceLevel != targetExperience || player.experience > 0.01F) {
                return player;
            }
        }
        return null;
    }

    private boolean canRun() {
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    public boolean roundUpToNextLevel(EntityPlayer player) {
        if (storedExperience <= 0) {
            return false;
        }
        int expInCurrentLevel = (int) (player.experience * player.xpBarCap());
        if (expInCurrentLevel > 0) {
            int expToGive = Math.min(storedExperience, ExperienceUtils.getExpNeededForNextLevel(player));
            player.addExperience(expToGive);
            storedExperience -= expToGive;
            return true;
        }
        return false;
    }

    private boolean canInteract(EntityPlayer player) {
        return player != null && (!ownerOnly || getOwnerUuid() == null || getOwnerUuid().equals(player.getUniqueID()));
    }

    public boolean hasPortableData() {
        return hasNonDefaultMachineSettings()
                || storedExperience != 0
                || targetExperience != 0
                || collectExperience
                || ownerOnly;
    }

    public NBTTagCompound writePortableData(NBTTagCompound compound) {
        writeMachineStateToNbt(compound);
        writeExperienceHolderSettings(compound);
        return compound;
    }

    public void readPortableData(NBTTagCompound compound) {
        readMachineStateFromNbt(compound);
        readExperienceHolderSettings(compound);
        getFluidState().setCapacity(Integer.MAX_VALUE);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        writeExperienceHolderSettings(compound);
        return compound;
    }

    private void writeExperienceHolderSettings(NBTTagCompound compound) {
        compound.setInteger("StoredExperience", storedExperience);
        compound.setInteger("TargetExperience", targetExperience);
        compound.setBoolean("OwnerOnly", ownerOnly);
        compound.setBoolean("CollectExperience", collectExperience);
        compound.setBoolean("ShowParticles", showParticles);
        compound.setInteger("exp", storedExperience);
        compound.setInteger("targetExp", targetExperience);
        compound.setBoolean("ownerOnly", ownerOnly);
        compound.setBoolean("collectExp", collectExperience);
        compound.setBoolean("showParticles", showParticles);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readExperienceHolderSettings(compound);
        getFluidState().setCapacity(Integer.MAX_VALUE);
    }

    private void readExperienceHolderSettings(NBTTagCompound compound) {
        storedExperience = compound.getInteger("StoredExperience");
        targetExperience = compound.getInteger("TargetExperience");
        ownerOnly = compound.getBoolean("OwnerOnly");
        collectExperience = compound.getBoolean("CollectExperience");
        showParticles = !compound.hasKey("ShowParticles") || compound.getBoolean("ShowParticles");
        if (compound.hasKey("exp")) {
            storedExperience = compound.getInteger("exp");
        }
        if (compound.hasKey("targetExp")) {
            targetExperience = compound.getInteger("targetExp");
        }
        if (compound.hasKey("ownerOnly")) {
            ownerOnly = compound.getBoolean("ownerOnly");
        }
        if (compound.hasKey("collectExp")) {
            collectExperience = compound.getBoolean("collectExp");
        }
        if (compound.hasKey("showParticles")) {
            showParticles = compound.getBoolean("showParticles");
        }
    }

    private boolean hasNonDefaultMachineSettings() {
        return getTickSpeed() != 20
                || hasNonDefaultArea()
                || getRedstoneState().getMode() != MachineRedstoneState.RedstoneMode.PULSE
                || getRedstoneState().isPulsed()
                || getRedstoneState().isReceivingRedstone();
    }

    private boolean hasNonDefaultArea() {
        EnumFacing facing = getDefaultAreaFacing();
        return getAreaState().getXRadius() != 0.0D
                || getAreaState().getYRadius() != 0.0D
                || getAreaState().getZRadius() != 0.0D
                || getAreaState().isRenderArea()
                || !isAreaOffset(facing);
    }

    private EnumFacing getDefaultAreaFacing() {
        if (world != null && pos != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getPropertyKeys().contains(BlockMachineBase.FACING)) {
                return state.getValue(BlockMachineBase.FACING).getOpposite();
            }
        }
        return EnumFacing.byIndex(getDirection()).getOpposite();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) experienceFluidTank;
        }
        return super.getCapability(capability, facing);
    }
}

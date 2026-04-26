package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.common.util.ExperienceUtils;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class TileExperienceHolder extends TileMachineBase implements ITickable {

    private int storedExperience;
    private int targetExperience;
    private boolean ownerOnly;
    private boolean collectExperience;
    private boolean showParticles = true;

    public TileExperienceHolder() {
        setTickSpeed(20);
        getAreaState().setArea(2.0D, 2.0D, 2.0D);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        if (!shouldRunTimedMachine()) {
            return;
        }

        boolean changed = false;
        if (collectExperience) {
            changed = collectExperienceOrbs();
        }
        changed |= balanceNearbyPlayers();
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

    public int addExperience(int amount) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = Integer.MAX_VALUE - storedExperience < amount ? Integer.MAX_VALUE - storedExperience : amount;
        storedExperience += accepted;
        return amount - accepted;
    }

    public int removeExperience(int amount) {
        int removed = Math.min(storedExperience, Math.max(0, amount));
        storedExperience -= removed;
        return removed;
    }

    public void storeExperience(EntityPlayer player, int levels) {
        if (!canInteract(player)) {
            return;
        }
        int toStore = levels < 0
                ? ExperienceUtils.getPlayerTotalExperience(player)
                : ExperienceUtils.pointsForLevels(player, levels);
        int removed = ExperienceUtils.removePoints(player, toStore);
        int leftover = addExperience(removed);
        if (leftover > 0) {
            player.addExperience(leftover);
        }
    }

    public void extractExperience(EntityPlayer player, int levels) {
        if (!canInteract(player) || storedExperience <= 0) {
            return;
        }
        int targetPoints = levels < 0
                ? storedExperience
                : Math.max(1, ExperienceUtils.getTotalExperienceForLevel(player.experienceLevel + levels) - ExperienceUtils.getPlayerTotalExperience(player));
        int extracted = removeExperience(targetPoints);
        if (extracted > 0) {
            player.addExperience(extracted);
        }
    }

    private boolean balanceNearbyPlayers() {
        if (targetExperience <= 0) {
            return false;
        }
        AxisAlignedBB area = getAreaState().createArea(pos);
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, area);
        for (EntityPlayer player : players) {
            if (!canInteract(player)) {
                continue;
            }
            int playerLevel = player.experienceLevel;
            if (playerLevel > targetExperience || (playerLevel == targetExperience && player.experience > 0.01F)) {
                int before = storedExperience;
                storeExperience(player, 1);
                return before != storedExperience;
            }
            if (playerLevel < targetExperience && storedExperience > 0) {
                int before = storedExperience;
                extractExperience(player, 1);
                return before != storedExperience;
            }
        }
        return false;
    }

    private boolean collectExperienceOrbs() {
        AxisAlignedBB area = getAreaState().createArea(pos);
        List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(EntityXPOrb.class, area);
        boolean changed = false;
        for (EntityXPOrb orb : orbs) {
            addExperience(orb.xpValue);
            orb.setDead();
            changed = true;
        }
        return changed;
    }

    private boolean canInteract(EntityPlayer player) {
        return player != null && (!ownerOnly || getOwnerUuid() == null || getOwnerUuid().equals(player.getUniqueID()));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("StoredExperience", storedExperience);
        compound.setInteger("TargetExperience", targetExperience);
        compound.setBoolean("OwnerOnly", ownerOnly);
        compound.setBoolean("CollectExperience", collectExperience);
        compound.setBoolean("ShowParticles", showParticles);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        storedExperience = compound.getInteger("StoredExperience");
        targetExperience = compound.getInteger("TargetExperience");
        ownerOnly = compound.getBoolean("OwnerOnly");
        collectExperience = compound.getBoolean("CollectExperience");
        showParticles = !compound.hasKey("ShowParticles") || compound.getBoolean("ShowParticles");
    }
}

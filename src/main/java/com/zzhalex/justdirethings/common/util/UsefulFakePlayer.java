package com.zzhalex.justdirethings.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.util.CooldownTracker;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;

public class UsefulFakePlayer extends FakePlayer {

    private double reach;

    public UsefulFakePlayer(WorldServer world, GameProfile name) {
        super(world, name);
        setReach(getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue());
    }

    public void fakeupdateUsingItem(ItemStack itemStack) {
        updateItemUse(itemStack, getItemInUseCount());
    }

    public void drawParticles(MinecraftServer server, ItemStack itemStack) {
        // 1.12 server-side fake player parity does not need the 1.20 particle path.
    }

    @Override
    protected CooldownTracker createCooldownTracker() {
        return new CooldownTracker();
    }

    @Override
    public net.minecraft.entity.Entity changeDimension(int dimension, ITeleporter teleporter) {
        return createPlayer((WorldServer) world, getGameProfile());
    }

    public double getReach() {
        return reach;
    }

    public void setReach(double reach) {
        this.reach = reach;
    }

    public static UsefulFakePlayer createPlayer(WorldServer world, GameProfile profile) {
        return new UsefulFakePlayer(world, profile);
    }
}

package com.zzhalex.justdirethings.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class TimeCrystalParticles {

    private TimeCrystalParticles() {
    }

    public static void spawnCharge(World world, double startX, double startY, double startZ, double targetX, double targetY, double targetZ, float red, float green, float blue) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.effectRenderer == null) {
            return;
        }
        if (world == null) {
            world = minecraft.world;
        }
        if (world == null) {
            return;
        }
        Particle particle = new TimeCrystalChargeParticle(world, startX, startY, startZ, targetX, targetY, targetZ, red, green, blue);
        minecraft.effectRenderer.addEffect(particle);
    }

    public static void spawnItemFlow(World world, double startX, double startY, double startZ, double targetX, double targetY, double targetZ, net.minecraft.item.ItemStack stack, int ticksPerBlock) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.effectRenderer == null || stack == null || stack.isEmpty()) {
            return;
        }
        if (world == null) {
            world = minecraft.world;
        }
        if (world == null) {
            return;
        }
        minecraft.effectRenderer.addEffect(new ItemFlowParticle(world, startX, startY, startZ, targetX, targetY, targetZ, stack, ticksPerBlock));
    }
}

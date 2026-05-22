package com.zzhalex.justdirethings.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class TimeCrystalChargeParticle extends Particle {

    public TimeCrystalChargeParticle(World world, double x, double y, double z, double targetX, double targetY, double targetZ, float red, float green, float blue) {
        super(world, x, y, z, 0.025D, 0.025D, 0.025D);
        setRBGColorF(red, green, blue);
        setAlphaF(0.5F);
        setMaxAge(120);
        particleScale = 0.05F + (0.025F - 0.05F) * rand.nextFloat();
        motionX = (targetX - x) / particleMaxAge;
        motionY = (targetY - y) / particleMaxAge;
        motionZ = (targetZ - z) / particleMaxAge;
        canCollide = false;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }
        move(motionX, motionY, motionZ);
    }
}
